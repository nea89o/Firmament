package moe.nea.firmament.util.mc

import com.mojang.serialization.Codec
import java.util.*
import kotlin.jvm.optionals.getOrNull
import net.minecraft.core.Holder
import net.minecraft.core.TypedInstance
import net.minecraft.core.component.*
import net.minecraft.resources.ResourceKey
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.Items
import net.minecraft.world.level.ItemLike
import moe.nea.firmament.events.ComponentsLoadedEvent

@RequiresOptIn
annotation class RequiresComponents

interface DataComponentAccessor {
	fun <T : Any> getWithDefault(prototype: DataComponentGetter, component: DataComponentType<T>): T?
	val item: Holder<Item>
	val itemId: ResourceKey<Item>
		get() = item.unwrapKey().orElseThrow()
	val count: Int

	fun maybeItemStack(): ItemStack? = null

	fun <T : Any> get(component: DataComponentType<T>): T? {
		return getWithDefault(NullData, component)
	}

	@RequiresComponents
	fun <T : Any> getWithDefault(component: DataComponentType<T>): T? {
		return getWithDefault(item.components(), component)
	}

}

@RequiresComponents
fun DataComponentAccessor.defaultItemStack() = item.value().defaultInstance

fun DataComponentAccessor.isEmpty() = count == 0 || isItem(Items.AIR)
fun DataComponentAccessor.isItem(item: ItemLike): Boolean = this.item.`is`(item.asItem().builtInRegistryHolder())
fun ItemStack.lazy() = LazyItemStack.fromItemStack(this)
open class ItemStackAccessor(val itemStack: ItemStack) : DataComponentAccessor {

	override fun <T : Any> getWithDefault(prototype: DataComponentGetter, component: DataComponentType<T>): T? {
		return itemStack.componentsPatch.get(prototype, component)
	}

	override fun maybeItemStack(): ItemStack = itemStack

	override val item: Holder<Item>
		get() = itemStack.typeHolder()
	override val count: Int
		get() = itemStack.count
}

fun ItemStack.accessor(): DataComponentAccessor = ItemStackAccessor(this)
fun ItemStack.mutator(): DataComponentMutator = object : ItemStackAccessor(this), DataComponentMutator {
	override fun <T : Any> set(component: DataComponentType<T>, data: T) {
		itemStack.set(component, data)
	}

	override fun remove(type: DataComponentType<*>) {
		itemStack.remove(type)
	}
}

interface DataComponentSetter {
	fun <T : Any> set(component: DataComponentType<T>, data: T)
	fun remove(type: DataComponentType<*>)
}

interface DataComponentMutator : DataComponentAccessor, DataComponentSetter

class DataComponentPatchAccessor(
	override val item: Holder<Item>,
	val patch: DataComponentPatch
) : DataComponentAccessor {
	override fun <T : Any> getWithDefault(prototype: DataComponentGetter, component: DataComponentType<T>): T? {
		return patch.get(prototype, component)
	}

	override val count: Int
		get() = 1
}

class MutableItemTemplate(
	override val item: Holder<Item>,
	override var count: Int,
	var components: DataComponentPatch.Builder,
) : DataComponentMutator {
	override fun <T : Any> set(component: DataComponentType<T>, data: T) {
		components.set(component, data)
	}

	override fun remove(type: DataComponentType<*>) {
		components.remove(type)
	}

	@RequiresComponents
	fun collapseDefaults() = collapseDefaults(item.components())

	fun collapseDefaults(prototype: DataComponentMap) {
		components = PatchedDataComponentMap.fromPatch(prototype, components.build())
			.asPatch()
			.toBuilder()
	}

	override fun <T : Any> getWithDefault(prototype: DataComponentGetter, component: DataComponentType<T>): T? {
		return components.build().get(prototype, component)
	}

	fun finishNonEmpty() = ItemStackTemplate(item, count, components.build())

	fun finish() = LazyItemStack.fromMutableTemplate(this)
}

fun DataComponentPatch.toBuilder(): DataComponentPatch.Builder {
	val builder = DataComponentPatch.builder()
	for ((k, v) in entrySet()) {
		if (v.isEmpty)
			builder.remove(k)
		else
			@Suppress("UNCHECKED_CAST")
			builder.set(k as DataComponentType<Any>, v.get())
	}
	return builder
}

object NullData : DataComponentGetter {
	override fun <T : Any> get(type: DataComponentType<out T>): T? {
		return null
	}
}

open class LazyItemStack private constructor() : DataComponentAccessor, TypedInstance<Item> {
	companion object {
		fun fromItemStack(itemStack: ItemStack) = LazyItemStack().also { it._itemStack = itemStack }
		fun fromTemplate(template: ItemStackTemplate?) = LazyItemStack().also { it._template = template }
		fun fromMutableTemplate(template: MutableItemTemplate) =
			if (template.isEmpty()) empty()
			else fromTemplate(template.finishNonEmpty())

		fun build(item: ItemLike, builder: DataComponentMutator.() -> Unit = {}) =
			build(item.asItem().builtInRegistryHolder(), builder)

		fun build(item: Holder<Item>, builder: DataComponentMutator.() -> Unit = {}) =
			fromMutableTemplate(
				MutableItemTemplate(item, 1, DataComponentPatch.builder())
					.also(builder)
			)

		fun empty() = LazyItemStack()

		val CODEC: Codec<LazyItemStack> = ExtraCodecs.optionalEmptyMap(ItemStackTemplate.CODEC)
			.xmap(
				{ fromTemplate(it.getOrNull()) },
				{ Optional.ofNullable(it.template()) })
	}

	fun intoMutable(): MutableItemTemplate {
		_template?.let { template ->
			return MutableItemTemplate(template.typeHolder(), template.count, template.components.toBuilder())
		}
		_itemStack?.let { stack ->
			return MutableItemTemplate(stack.typeHolder(), stack.count, stack.componentsPatch.toBuilder())
		}
		return MutableItemTemplate(typeHolder(), 1, DataComponentPatch.builder())
	}

	private var _itemStack: ItemStack? = null
	private var _template: ItemStackTemplate? = null
	private var generation = ComponentsLoadedEvent.generation

	fun withMutations(builder: MutableItemTemplate.() -> Unit) =
		intoMutable().also(builder).finish()

	fun template(): ItemStackTemplate? {
		if (_template == null)
			_template = _itemStack
				?.takeIf { !it.isEmpty }
				?.let { ItemStackTemplate.fromNonEmptyStack(it) }
		return _template
	}

	@RequiresComponents
	fun recreate(): ItemStack {
		return (_template?.create() ?: ItemStack.EMPTY)
			.also { this._itemStack = it }
	}

	@RequiresComponents
	fun copiedUpgrade(): ItemStack {
		return upgrade().copy()
	}

	@RequiresComponents
	fun upgrade(): ItemStack {
		tryDecay()
		return _itemStack ?: recreate()
	}

	fun tryDecay() {
		if (ComponentsLoadedEvent.generation != generation)
			decay()
	}


	fun decay() {
		template()
		_itemStack = null
	}

	override val item: Holder<Item>
		get() = _itemStack?.typeHolder() ?: _template?.typeHolder() ?: Items.AIR.builtInRegistryHolder()
	override val count: Int
		get() = _itemStack?.count ?: _template?.count ?: 0

	override fun <T : Any> getWithDefault(prototype: DataComponentGetter, component: DataComponentType<T>): T? {
		tryDecay()
		val patch = _itemStack?.componentsPatch ?: _template?.components ?: return null
		return DataComponentPatchAccessor(item, patch).getWithDefault(prototype, component)
	}

	override fun typeHolder(): Holder<Item> = item
	fun withCount(count: Int): LazyItemStack {
		return LazyItemStack().also {
			it._itemStack = _itemStack?.copyWithCount(count)
			it._template = _template?.withCount(count)
		}
	}

	override fun maybeItemStack(): ItemStack? {
		return _itemStack
	}
}

