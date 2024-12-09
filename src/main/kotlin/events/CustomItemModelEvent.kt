package moe.nea.firmament.events

import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

// TODO: assert an order on these events
data class CustomItemModelEvent(
	val itemStack: ItemStack,
	var overrideModel: Identifier? = null,
) : FirmamentEvent() {
	companion object : FirmamentEventBus<CustomItemModelEvent>() {
		@JvmStatic
		fun getModelIdentifier(itemStack: ItemStack?): Identifier? {
			// TODO: Re-add memoization and add an error / warning if the model does not exist
			if (itemStack == null) return null
			return publish(CustomItemModelEvent(itemStack)).overrideModel
		}
	}

	fun overrideIfExists(overrideModel: Identifier) {
		this.overrideModel = overrideModel
	}
}
