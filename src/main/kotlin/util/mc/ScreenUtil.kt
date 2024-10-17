package moe.nea.firmament.util.mc

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.slot.Slot

object ScreenUtil {
	private var lastScreen: Screen? = null
	private var slotsByIndex: Map<SlotIndex, Slot> = mapOf()

	data class SlotIndex(val index: Int, val isPlayerInventory: Boolean)

	fun Screen.getSlotsByIndex(): Map<SlotIndex, Slot> {
		if (this !is HandledScreen<*>) return mapOf()
		if (lastScreen === this) return slotsByIndex
		lastScreen = this
		slotsByIndex = this.screenHandler.slots.associate {
			SlotIndex(it.index, it.inventory is PlayerInventory) to it
		}
		return slotsByIndex
	}

	fun Screen.getSlotByIndex( index: Int, isPlayerInventory: Boolean): Slot? =
		getSlotsByIndex()[SlotIndex(index, isPlayerInventory)]
}
