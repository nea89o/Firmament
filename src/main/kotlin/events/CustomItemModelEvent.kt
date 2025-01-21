package moe.nea.firmament.events

import java.util.Optional
import kotlin.jvm.optionals.getOrNull
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import moe.nea.firmament.util.collections.WeakCache
import moe.nea.firmament.util.mc.IntrospectableItemModelManager

// TODO: assert an order on these events
data class CustomItemModelEvent(
	val itemStack: ItemStack,
	val itemModelManager: IntrospectableItemModelManager,
	var overrideModel: Identifier? = null,
) : FirmamentEvent() {
	companion object : FirmamentEventBus<CustomItemModelEvent>() {
		val cache = WeakCache.memoize("ItemModelIdentifier", ::getModelIdentifier0)

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
