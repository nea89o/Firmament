package moe.nea.firmament.gui

import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext
import io.github.notenoughupdates.moulconfig.gui.KeyboardEvent
import io.github.notenoughupdates.moulconfig.gui.MouseEvent
import java.util.function.BiFunction
import java.util.function.Supplier
import kotlin.time.Duration
import moe.nea.firmament.util.TimeMark

class FirmHoverComponent(
	val child: GuiComponent,
	val hoverLines: Supplier<List<String>>,
	val hoverDelay: Duration,
) : GuiComponent() {
	override fun getWidth(): Int {
		return child.width
	}

	override fun getHeight(): Int {
		return child.height
	}

	override fun <T : Any?> foldChildren(
		initial: T,
		visitor: BiFunction<GuiComponent, T, T>
	): T {
		return visitor.apply(child, initial)
	}

	override fun render(context: GuiImmediateContext) {
		if (context.isHovered && (permaHover || lastMouseMove.passedTime() > hoverDelay)) {
			context.renderContext.scheduleDrawTooltip(context.mouseX, context.mouseY, hoverLines.get())
			permaHover = true
		} else {
			permaHover = false
		}
		if (!context.isHovered) {
			lastMouseMove = TimeMark.now()
		}
		child.render(context)

	}

	var permaHover = false
	var lastMouseMove = TimeMark.farPast()

	override fun mouseEvent(mouseEvent: MouseEvent, context: GuiImmediateContext): Boolean {
		if (mouseEvent is MouseEvent.Move) {
			lastMouseMove = TimeMark.now()
		}
		return child.mouseEvent(mouseEvent, context)
	}

	override fun keyboardEvent(event: KeyboardEvent, context: GuiImmediateContext): Boolean {
		return child.keyboardEvent(event, context)
	}
}
