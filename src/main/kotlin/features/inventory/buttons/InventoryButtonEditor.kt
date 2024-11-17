package moe.nea.firmament.features.inventory.buttons

import io.github.notenoughupdates.moulconfig.common.IItemStack
import io.github.notenoughupdates.moulconfig.platform.ModernItemStack
import io.github.notenoughupdates.moulconfig.xml.Bind
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import org.lwjgl.glfw.GLFW
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec2f
import moe.nea.firmament.util.ClipboardUtils
import moe.nea.firmament.util.FragmentGuiScreen
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.MoulConfigUtils
import moe.nea.firmament.util.tr

class InventoryButtonEditor(
	val lastGuiRect: Rectangle,
) : FragmentGuiScreen() {
	inner class Editor(val originalButton: InventoryButton) {
		@field:Bind
		var command: String = originalButton.command ?: ""

		@field:Bind
		var icon: String = originalButton.icon ?: ""

		@Bind
		fun getItemIcon(): IItemStack {
			save()
			return ModernItemStack.of(InventoryButton.getItemForName(icon))
		}

		@Bind
		fun delete() {
			buttons.removeIf { it === originalButton }
			popup = null
		}

		fun save() {
			originalButton.icon = icon
			originalButton.command = command
		}
	}

	var buttons: MutableList<InventoryButton> =
		InventoryButtons.DConfig.data.buttons.map { it.copy() }.toMutableList()

	override fun close() {
		InventoryButtons.DConfig.data.buttons = buttons
		InventoryButtons.DConfig.markDirty()
		super.close()
	}

	override fun init() {
		super.init()
		addDrawableChild(
			ButtonWidget.builder(Text.translatable("firmament.inventory-buttons.load-preset")) {
				val t = ClipboardUtils.getTextContents()
				val newButtons = InventoryButtonTemplates.loadTemplate(t)
				if (newButtons != null)
					buttons = moveButtons(newButtons.map { it.copy(command = it.command?.removePrefix("/")) })
			}
				.position(lastGuiRect.minX + 10, lastGuiRect.minY + 35)
				.width(lastGuiRect.width - 20)
				.build()
		)
		addDrawableChild(
			ButtonWidget.builder(Text.translatable("firmament.inventory-buttons.save-preset")) {
				ClipboardUtils.setTextContent(InventoryButtonTemplates.saveTemplate(buttons))
			}
				.position(lastGuiRect.minX + 10, lastGuiRect.minY + 60)
				.width(lastGuiRect.width - 20)
				.build()
		)
	}

	private fun moveButtons(buttons: List<InventoryButton>): MutableList<InventoryButton> {
		val newButtons: MutableList<InventoryButton> = ArrayList(buttons.size)
		val movedButtons = mutableListOf<InventoryButton>()
		for (button in buttons) {
			if ((!button.anchorBottom && !button.anchorRight && button.x > 0 && button.y > 0)) {
				MC.sendChat(tr("firmament.inventory-buttons.button-moved",
				               "One of your imported buttons intersects with the inventory and has been moved to the top left."))
				movedButtons.add(button.copy(
					x = 0,
					y = -InventoryButton.dimensions.width,
					anchorRight = false,
					anchorBottom = false
				))
			} else {
				newButtons.add(button)
			}
		}
		var i = 0
		val zeroRect = Rectangle(0, 0, 1, 1)
		for (movedButton in movedButtons) {
			fun getPosition(button: InventoryButton, index: Int) =
				button.copy(x = (index % 10) * InventoryButton.dimensions.width,
				            y = (index / 10) * -InventoryButton.dimensions.height,
				            anchorRight = false, anchorBottom = false)
			while (true) {
				val newPos = getPosition(movedButton, i++)
				val newBounds = newPos.getBounds(zeroRect)
				if (newButtons.none { it.getBounds(zeroRect).intersects(newBounds) }) {
					newButtons.add(newPos)
					break
				}
			}
		}
		return newButtons
	}

	override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
		super.render(context, mouseX, mouseY, delta)
		context.matrices.push()
		context.matrices.translate(0f, 0f, -10f)
		context.fill(lastGuiRect.minX, lastGuiRect.minY, lastGuiRect.maxX, lastGuiRect.maxY, -1)
		context.matrices.pop()
		for (button in buttons) {
			val buttonPosition = button.getBounds(lastGuiRect)
			context.matrices.push()
			context.matrices.translate(buttonPosition.minX.toFloat(), buttonPosition.minY.toFloat(), 0F)
			button.render(context)
			context.matrices.pop()
		}
	}

	override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
		if (super.keyPressed(keyCode, scanCode, modifiers)) return true
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			close()
			return true
		}
		return false
	}

	override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (super.mouseReleased(mouseX, mouseY, button)) return true
		val clickedButton = buttons.firstOrNull { it.getBounds(lastGuiRect).contains(Point(mouseX, mouseY)) }
		if (clickedButton != null && !justPerformedAClickAction) {
			createPopup(MoulConfigUtils.loadGui("button_editor_fragment", Editor(clickedButton)), Point(mouseX, mouseY))
			return true
		}
		justPerformedAClickAction = false
		lastDraggedButton = null
		return false
	}

	override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
		if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true

		if (initialDragMousePosition.distanceSquared(Vec2f(mouseX.toFloat(), mouseY.toFloat())) >= 4 * 4) {
			initialDragMousePosition = Vec2f(-10F, -10F)
			lastDraggedButton?.let { dragging ->
				justPerformedAClickAction = true
				val (anchorRight, anchorBottom, offsetX, offsetY) = getCoordsForMouse(mouseX.toInt(), mouseY.toInt())
					?: return true
				dragging.x = offsetX
				dragging.y = offsetY
				dragging.anchorRight = anchorRight
				dragging.anchorBottom = anchorBottom
			}
		}
		return false
	}

	var lastDraggedButton: InventoryButton? = null
	var justPerformedAClickAction = false
	var initialDragMousePosition = Vec2f(-10F, -10F)

	data class AnchoredCoords(
		val anchorRight: Boolean,
		val anchorBottom: Boolean,
		val offsetX: Int,
		val offsetY: Int,
	)

	fun getCoordsForMouse(mx: Int, my: Int): AnchoredCoords? {
		if (lastGuiRect.contains(mx, my) || lastGuiRect.contains(
				Point(
					mx + InventoryButton.dimensions.width,
					my + InventoryButton.dimensions.height,
				)
			)
		) return null

		val anchorRight = mx > lastGuiRect.maxX
		val anchorBottom = my > lastGuiRect.maxY
		var offsetX = mx - if (anchorRight) lastGuiRect.maxX else lastGuiRect.minX
		var offsetY = my - if (anchorBottom) lastGuiRect.maxY else lastGuiRect.minY
		if (InputUtil.isKeyPressed(MC.window.handle, InputUtil.GLFW_KEY_LEFT_SHIFT)) {
			offsetX = MathHelper.floor(offsetX / 20F) * 20
			offsetY = MathHelper.floor(offsetY / 20F) * 20
		}
		return AnchoredCoords(anchorRight, anchorBottom, offsetX, offsetY)
	}

	override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
		if (super.mouseClicked(mouseX, mouseY, button)) return true
		val clickedButton = buttons.firstOrNull { it.getBounds(lastGuiRect).contains(Point(mouseX, mouseY)) }
		if (clickedButton != null) {
			lastDraggedButton = clickedButton
			initialDragMousePosition = Vec2f(mouseX.toFloat(), mouseY.toFloat())
			return true
		}
		val mx = mouseX.toInt()
		val my = mouseY.toInt()
		val (anchorRight, anchorBottom, offsetX, offsetY) = getCoordsForMouse(mx, my) ?: return true
		buttons.add(InventoryButton(offsetX, offsetY, anchorRight, anchorBottom, null, null))
		justPerformedAClickAction = true
		return true
	}

}
