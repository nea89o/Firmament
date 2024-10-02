package moe.nea.firmament.util.customgui

import me.shedaniel.math.Rectangle
import net.minecraft.client.gui.DrawContext
import net.minecraft.screen.slot.Slot
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HandledScreenPushREIEvent

abstract class CustomGui {

	abstract fun getBounds(): List<Rectangle>

	open fun moveSlot(slot: Slot) {
		// TODO: return a Pair maybe? worth an investigation
	}

	companion object {
		@Subscribe
		fun onExclusionZone(event: HandledScreenPushREIEvent) {
			val customGui = event.screen.customGui ?: return
			event.rectangles.addAll(customGui.getBounds())
		}
	}

	open fun render(
		drawContext: DrawContext,
		delta: Float,
		mouseX: Int,
		mouseY: Int
	) {
	}

	open fun mouseClick(mouseX: Double, mouseY: Double, button: Int): Boolean {
		return false
	}

	open fun afterSlotRender(context: DrawContext, slot: Slot) {}
	open fun beforeSlotRender(context: DrawContext, slot: Slot) {}
	open fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
		return false
	}

	open fun isClickOutsideBounds(mouseX: Double, mouseY: Double): Boolean {
		return getBounds().none { it.contains(mouseX, mouseY) }
	}

	open fun isPointWithinBounds(
		x: Int,
		y: Int,
		width: Int,
		height: Int,
		pointX: Double,
		pointY: Double,
	): Boolean {
		return getBounds().any { it.contains(pointX, pointY) } &&
			Rectangle(x, y, width, height).contains(pointX, pointY)
	}

	open fun isPointOverSlot(slot: Slot, xOffset: Int, yOffset: Int, pointX: Double, pointY: Double): Boolean {
		return isPointWithinBounds(slot.x + xOffset, slot.y + yOffset, 16, 16, pointX, pointY)
	}

	open fun onInit() {}
	open fun shouldDrawForeground(): Boolean {
		return true
	}

	open fun onVoluntaryExit(): Boolean {
		return true
	}

	open fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
		return false
	}

	open fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		return false
	}
}
