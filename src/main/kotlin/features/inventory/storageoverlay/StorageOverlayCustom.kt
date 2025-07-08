package moe.nea.firmament.features.inventory.storageoverlay

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.slot.Slot
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen
import moe.nea.firmament.util.customgui.CustomGui
import moe.nea.firmament.util.focusedItemStack

class StorageOverlayCustom(
	val handler: StorageBackingHandle,
	val screen: GenericContainerScreen,
	val overview: StorageOverlayScreen,
) : CustomGui() {
	override fun onVoluntaryExit(): Boolean {
		overview.isExiting = true
		StorageOverlayScreen.resetScroll()
		return super.onVoluntaryExit()
	}

	override fun getBounds(): List<Rectangle> {
		return overview.getBounds()
	}

	override fun afterSlotRender(context: DrawContext, slot: Slot) {
		if (slot.inventory !is PlayerInventory)
			context.disableScissor()
	}

	override fun beforeSlotRender(context: DrawContext, slot: Slot) {
		if (slot.inventory !is PlayerInventory)
			overview.createScissors(context)
	}

	override fun onInit() {
		overview.init(MinecraftClient.getInstance(), screen.width, screen.height)
		overview.init()
		screen as AccessorHandledScreen
		screen.x_Firmament = overview.measurements.x
		screen.y_Firmament = overview.measurements.y
		screen.backgroundWidth_Firmament = overview.measurements.totalWidth
		screen.backgroundHeight_Firmament = overview.measurements.totalHeight
	}

	override fun isPointOverSlot(slot: Slot, xOffset: Int, yOffset: Int, pointX: Double, pointY: Double): Boolean {
		if (!super.isPointOverSlot(slot, xOffset, yOffset, pointX, pointY))
			return false
		if (slot.inventory !is PlayerInventory) {
			if (!overview.getScrollPanelInner().contains(pointX, pointY))
				return false
		}
		return true
	}

	override fun shouldDrawForeground(): Boolean {
		return false
	}

	override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
		return overview.mouseReleased(mouseX, mouseY, button)
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		return overview.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
	}

	override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		return overview.keyReleased(keyCode, scanCode, modifiers)
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		return overview.keyPressed(keyCode, scanCode, modifiers)
	}

	override fun charTyped(chr: Char, modifiers: Int): Boolean {
		return overview.charTyped(chr, modifiers)
	}

	override fun mouseClick(mouseX: Double, mouseY: Double, button: Int): Boolean {
		return overview.mouseClicked(mouseX, mouseY, button, (handler as? StorageBackingHandle.Page)?.storagePageSlot)
	}

	override fun render(drawContext: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
		overview.drawBackgrounds(drawContext)
		overview.drawPages(drawContext,
		                   mouseX,
		                   mouseY,
		                   delta,
		                   (handler as? StorageBackingHandle.Page)?.storagePageSlot,
		                   screen.screenHandler.slots.take(screen.screenHandler.rows * 9).drop(9),
		                   Point((screen as AccessorHandledScreen).x_Firmament, screen.y_Firmament))
		overview.drawScrollBar(drawContext)
		overview.drawControls(drawContext, mouseX, mouseY)
	}

	override fun moveSlot(slot: Slot) {
		val index = slot.index
		if (index in 0..<36) {
			val (x, y) = overview.getPlayerInventorySlotPosition(index)
			slot.x = x - (screen as AccessorHandledScreen).x_Firmament
			slot.y = y - screen.y_Firmament
		} else {
			slot.x = -100000
			slot.y = -100000
		}
	}

	override fun mouseScrolled(
		mouseX: Double,
		mouseY: Double,
		horizontalAmount: Double,
		verticalAmount: Double
	): Boolean {
		if (screen.focusedItemStack != null && StorageOverlay.TConfig.itemsBlockScrolling)
			return false
		return overview.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
	}
}
