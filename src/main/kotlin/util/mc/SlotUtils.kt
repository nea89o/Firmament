package moe.nea.firmament.util.mc

import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import moe.nea.firmament.util.MC

object SlotUtils {
	fun Slot.clickMiddleMouseButton(handler: ScreenHandler) {
		MC.interactionManager?.clickSlot(
			handler.syncId,
			this.id,
			2,
			SlotActionType.CLONE,
			MC.player
		)
	}

	fun Slot.swapWithHotBar(handler: ScreenHandler, hotbarIndex: Int) {
		MC.interactionManager?.clickSlot(
			handler.syncId, this.id,
			hotbarIndex, SlotActionType.SWAP,
			MC.player)
	}

	fun Slot.clickRightMouseButton(handler: ScreenHandler) {
		MC.interactionManager?.clickSlot(
			handler.syncId,
			this.id,
			1,
			SlotActionType.PICKUP,
			MC.player
		)
	}
}
