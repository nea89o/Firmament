package moe.nea.firmament.util.mc

import org.lwjgl.glfw.GLFW
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.inventory.ClickType
import moe.nea.firmament.util.MC

object SlotUtils {
	fun Slot.clickMiddleMouseButton(handler: AbstractContainerMenu) {
		MC.interactionManager?.handleInventoryMouseClick(
			handler.containerId,
			this.index,
			GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
			ClickType.CLONE,
			MC.player!!
		)
	}

	fun Slot.swapWithHotBar(handler: AbstractContainerMenu, hotbarIndex: Int) {
		MC.interactionManager?.handleInventoryMouseClick(
			handler.containerId, this.index,
			hotbarIndex, ClickType.SWAP,
			MC.player!!
		)
	}

	fun Slot.clickRightMouseButton(handler: AbstractContainerMenu) {
		MC.interactionManager?.handleInventoryMouseClick(
			handler.containerId,
			this.index,
			GLFW.GLFW_MOUSE_BUTTON_RIGHT,
			ClickType.PICKUP,
			MC.player!!
		)
	}

	fun Slot.clickLeftMouseButton(handler: AbstractContainerMenu) {
		MC.interactionManager?.handleInventoryMouseClick(
			handler.containerId,
			this.index,
			GLFW.GLFW_MOUSE_BUTTON_LEFT,
			ClickType.PICKUP,
			MC.player!!
		)
	}
}
