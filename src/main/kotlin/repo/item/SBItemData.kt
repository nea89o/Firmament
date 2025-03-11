package moe.nea.firmament.repo.item

import kotlin.collections.runningFold
import net.minecraft.item.ItemStack
import moe.nea.firmament.repo.RepoManager

class SBItemData {
	private var itemCache: ItemStack? = null

	private val data: MutableMap<SBItemProperty<*>, Any> = mutableMapOf()

	fun <T> getData(prop: SBItemProperty<T>): T? {
		return data[prop] as T?
	}

	fun <Prop : SBItemProperty<T>, T> set(property: Prop, data: T) {
		if (data != null) {
			(this.data as MutableMap<Any, Any>)[property] = data
		} else {
			this.data.remove(property)
		}
		itemCache = null
	}

	private fun <T> enhanceStack(stack: ItemStack, property: SBItemProperty.State<T>): ItemStack {
		val data = getData(property)
		return property.applyToStack(stack, this, data)
	}

	private fun createStack(): ItemStack {
		return SBItemProperty.allStates.fold(ItemStack.EMPTY) { stack, prop ->
			enhanceStack(stack, prop)
		}
	}

	fun debugStackCreation(): List<PartialStack<*>> {
		fun <T> combinedEnhanceStack(previous: PartialStack<*>, mod: SBItemProperty.State<T>): PartialStack<T> {
			val nextStack = enhanceStack(previous.stack.copy(), mod)
			return PartialStack(mod, getData(mod), nextStack)
		}
		return SBItemProperty.allStates
			.runningFold(
				PartialStack<Nothing>(null, null, ItemStack.EMPTY)) { stack: PartialStack<*>, prop ->
				combinedEnhanceStack(stack, prop)
			}
	}

	/**
	 * Creates an [ItemStack] based on the current properties. The returned item stack must not be modified by the
	 * caller.
	 */
	fun toImmutableStack(): ItemStack {
		var cached = itemCache
		if (cached == null) {
			cached = createStack()
			itemCache = cached
		}
		return cached
	}

	data class PartialStack<T>(
		val lastAppliedModifier: SBItemProperty<T>?,
		val data: T?,
		val stack: ItemStack,
	)

	companion object {
		/**
		 * Create an [SBItemData] from only the given characteristica. Any unspecified characteristica will be non-existent.
		 * If you want to compute all other properties based on the given properties, use [roundtrip].
		 */
		fun fromCharacteristica(
			vararg char: SBItemProperty.BoundState<*>
		): SBItemData {
			val store = SBItemData()
			char.forEach {
				it.applyTo(store)
			}
			return store
		}

		fun fromStack(itemStack: ItemStack): SBItemData {
			val store = SBItemData()
			store.loadFrom(itemStack)
			return store
		}
	}

	/**
	 * Creates a new [SBItemData] from the item stack this [SBItemData] produces. This will initialize all properties.
	 */
	fun roundtrip(): SBItemData {
		return fromStack(toImmutableStack())
	}

	/**
	 * Creates a new [SBItemData] with cheap inferences completed only by using data available in [io.github.moulberry.repo.data.NEUItem]. This is a cheaper version of [roundtrip], that does not create any [ItemStack]s, and preserves all properties already provided. Check if the property you need overrides [SBItemProperty.fromNeuItem].
	 */
	fun cheapInfer(): SBItemData {
		val neuItem = getData(SBItemId)?.let { RepoManager.getNEUItem(it) } ?: return this
		val store = SBItemData()
		SBItemProperty.allProperties.forEach {
			it.fromNeuItem(neuItem, this)
		}
		store.data.putAll(this.data)
		return store
	}

	private fun loadFrom(stack: ItemStack) {
		SBItemProperty.allProperties.forEach {
			loadModifier(stack, it)
		}
	}

	private fun <T> loadModifier(
		stack: ItemStack,
		modifier: SBItemProperty<T>
	) {
		val data = modifier.fromStack(stack, this) ?: return
		set(modifier, data)
	}
}
