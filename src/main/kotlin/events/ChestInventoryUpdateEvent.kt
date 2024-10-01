package moe.nea.firmament.events

import net.minecraft.item.ItemStack
import moe.nea.firmament.util.MC

sealed class ChestInventoryUpdateEvent : FirmamentEvent() {
	companion object : FirmamentEventBus<ChestInventoryUpdateEvent>()
	data class Single(val slot: Int, val stack: ItemStack) : ChestInventoryUpdateEvent()
	data class Multi(val contents: List<ItemStack>) : ChestInventoryUpdateEvent()
	val inventory = MC.screen
}
