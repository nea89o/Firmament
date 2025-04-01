package moe.nea.firmament.events

import java.util.Objects
import java.util.Optional
import kotlin.jvm.optionals.getOrNull
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import moe.nea.firmament.util.collections.WeakCache
import moe.nea.firmament.util.collections.WeakCache.CacheFunction
import moe.nea.firmament.util.mc.IntrospectableItemModelManager

// TODO: assert an order on these events
data class CustomItemModelEvent(
	val itemStack: ItemStack,
	val itemModelManager: IntrospectableItemModelManager,
	var overrideModel: Identifier? = null,
) : FirmamentEvent() {
	companion object : FirmamentEventBus<CustomItemModelEvent>() {
		val weakCache =
			object : WeakCache<ItemStack, IntrospectableItemModelManager, Optional<Identifier>>("ItemModelIdentifier") {
				override fun mkRef(
					key: ItemStack,
					extraData: IntrospectableItemModelManager
				): WeakCache<ItemStack, IntrospectableItemModelManager, Optional<Identifier>>.Ref {
					return IRef(key, extraData)
				}

				inner class IRef(weakInstance: ItemStack, data: IntrospectableItemModelManager) :
					Ref(weakInstance, data) {
					override fun shouldBeEvicted(): Boolean = false
					val isSimpleStack = weakInstance.componentChanges.isEmpty || (weakInstance.componentChanges.size() == 1 && weakInstance.get(
						DataComponentTypes.CUSTOM_DATA)?.isEmpty == true)
					val item = weakInstance.item
					override fun hashCode(): Int {
						if (isSimpleStack)
							return Objects.hash(item, extraData)
						return super.hashCode()
					}

					override fun equals(other: Any?): Boolean {
						if (other is IRef && isSimpleStack) {
							return other.isSimpleStack && item == other.item
						}
						return super.equals(other)
					}
				}
			}
		val cache = CacheFunction.WithExtraData(weakCache, ::getModelIdentifier0)

		@JvmStatic
		fun getModelIdentifier(itemStack: ItemStack?, itemModelManager: IntrospectableItemModelManager): Identifier? {
			if (itemStack == null) return null
			return cache.invoke(itemStack, itemModelManager).getOrNull()
		}

		fun getModelIdentifier0(
			itemStack: ItemStack,
			itemModelManager: IntrospectableItemModelManager
		): Optional<Identifier> {
			// TODO: add an error / warning if the model does not exist
			return Optional.ofNullable(publish(CustomItemModelEvent(itemStack, itemModelManager)).overrideModel)
		}
	}

	fun overrideIfExists(overrideModel: Identifier) {
		if (itemModelManager.hasModel_firmament(overrideModel))
			this.overrideModel = overrideModel
	}

	fun overrideIfEmpty(identifier: Identifier) {
		if (overrideModel == null)
			overrideModel = identifier
	}
}
