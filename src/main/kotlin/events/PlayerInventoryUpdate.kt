package moe.nea.firmament.events

import net.minecraft.item.ItemStack

sealed class PlayerInventoryUpdate : FirmamentEvent() {
	companion object : FirmamentEventBus<PlayerInventoryUpdate>()
	data class Single(val slot: Int, val stack: ItemStack) : PlayerInventoryUpdate() {
		override fun getOrNull(slot: Int): ItemStack? {
			if (slot == this.slot) return stack
			return null
		}

	}

	data class Multi(val contents: List<ItemStack>) : PlayerInventoryUpdate() {
		override fun getOrNull(slot: Int): ItemStack? {
			return contents.getOrNull(slot)
		}
	}

	abstract fun getOrNull(slot: Int): ItemStack?
}
