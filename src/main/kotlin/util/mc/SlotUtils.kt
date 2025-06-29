package moe.nea.firmament.util.mc

import org.lwjgl.glfw.GLFW
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import moe.nea.firmament.util.MC

object SlotUtils {
	fun Slot.clickMiddleMouseButton(handler: ScreenHandler) {
		MC.interactionManager?.clickSlot(
			handler.syncId,
			this.id,
			GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
			SlotActionType.CLONE,
			MC.player
		)
	}

	fun Slot.swapWithHotBar(handler: ScreenHandler, hotbarIndex: Int) {
		MC.interactionManager?.clickSlot(
			handler.syncId, this.id,
			hotbarIndex, SlotActionType.SWAP,
			MC.player
		)
	}

	fun Slot.clickRightMouseButton(handler: ScreenHandler) {
		MC.interactionManager?.clickSlot(
			handler.syncId,
			this.id,
			GLFW.GLFW_MOUSE_BUTTON_RIGHT,
			SlotActionType.PICKUP,
			MC.player
		)
	}

	fun Slot.clickLeftMouseButton(handler: ScreenHandler) {
		MC.interactionManager?.clickSlot(
			handler.syncId,
			this.id,
			GLFW.GLFW_MOUSE_BUTTON_LEFT,
			SlotActionType.PICKUP,
			MC.player
		)
	}
}
