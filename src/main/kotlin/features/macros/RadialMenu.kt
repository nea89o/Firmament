package moe.nea.firmament.features.macros

import me.shedaniel.math.Color
import org.joml.Vector2f
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import net.minecraft.client.gui.GuiGraphicsExtractor
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HudRenderEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.events.WorldKeyboardEvent
import moe.nea.firmament.events.WorldMouseMoveEvent
import moe.nea.firmament.features.macros.RadialMenuViewer.RadialMenu
import moe.nea.firmament.features.macros.RadialMenuViewer.RadialMenuOption
import moe.nea.firmament.keybindings.SavedKeyBinding
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.render.CustomRenderTypes
import moe.nea.firmament.util.render.drawLine
import moe.nea.firmament.util.render.gui.pip.RenderCircleProgressRenderer
import moe.nea.firmament.util.render.lerpAngle
import moe.nea.firmament.util.render.wrapAngle
import moe.nea.firmament.util.render.τ

object RadialMenuViewer {
	interface RadialMenu {
		val key: SavedKeyBinding
		val options: List<RadialMenuOption>
	}

	interface RadialMenuOption {
		val isEnabled: Boolean
		fun resolve()
		fun renderSlice(drawContext: GuiGraphicsExtractor)
	}

	var activeMenu: RadialMenu? = null
		set(value) {
			if (value?.options.isNullOrEmpty()) {
				field = null
			} else {
				field = value
			}
			delta = Vector2f(0F, 0F)
		}
	var delta = Vector2f(0F, 0F)
	val maxSelectionSize = 100F

	@Subscribe
	fun onMouseMotion(event: WorldMouseMoveEvent) {
		val menu = activeMenu ?: return
		event.cancel()
		delta.add(event.deltaX.toFloat(), event.deltaY.toFloat())
		val m = delta.lengthSquared()
		if (m > maxSelectionSize * maxSelectionSize) {
			delta.mul(maxSelectionSize / sqrt(m))
		}
	}

	val INNER_CIRCLE_RADIUS = 16

	@Subscribe
	fun onRender(event: HudRenderEvent) {
		val menu = activeMenu ?: return
		val mat = event.context.pose()
		mat.pushMatrix()
		mat.translate(
			(MC.window.guiScaledWidth) / 2F,
			(MC.window.guiScaledHeight) / 2F,
		)
		val sliceWidth = (τ / menu.options.size).toFloat()
		var selectedAngle = wrapAngle(atan2(delta.y, delta.x))
		if (delta.lengthSquared() < INNER_CIRCLE_RADIUS * INNER_CIRCLE_RADIUS)
			selectedAngle = Float.NaN
		for ((idx, option) in menu.options.withIndex()) {
			val range = (sliceWidth * idx)..(sliceWidth * (idx + 1))
			mat.pushMatrix()
			mat.scale(64F, 64F)
			val cutout = INNER_CIRCLE_RADIUS / 64F / 2
			RenderCircleProgressRenderer.extractCircularSlice(
				event.context,
				CustomRenderTypes.TRANSLUCENT_CIRCLE_GUI,
				0F, 1F, 0F, 1F,
				range,
				if (selectedAngle in range) 0x70A0A0A0 else 0x70FFFFFF,
				cutout
			)
			mat.popMatrix()
			mat.pushMatrix()
			val centreAngle = lerpAngle(range.start, range.endInclusive, 0.5F)
			val vec = Vector2f(cos(centreAngle), sin(centreAngle)).mul(40F)
			mat.translate(vec.x, vec.y)
			option.renderSlice(event.context)
			mat.popMatrix()
		}
		event.context.drawLine(1, 1, delta.x.toInt(), delta.y.toInt(), Color.ofOpaque(0x00FF00))
		mat.popMatrix()
	}

	@Subscribe
	fun onTick(event: TickEvent) {
		val menu = activeMenu ?: return
		if (!menu.key.isPressed(true)) {
			val angle = atan2(delta.y, delta.x)

			val choiceIndex = (wrapAngle(angle) * menu.options.size / τ).toInt()
			val choice = menu.options[choiceIndex]
			val selectedAny = delta.lengthSquared() > INNER_CIRCLE_RADIUS * INNER_CIRCLE_RADIUS
			activeMenu = null
			if (selectedAny)
				choice.resolve()
		}
	}

}

object RadialMacros {
	lateinit var wheels: List<MacroWheel>
		private set

	fun setWheels(wheels: List<MacroWheel>) {
		this.wheels = wheels
		RadialMenuViewer.activeMenu = null
	}

	@Subscribe
	fun onOpen(event: WorldKeyboardEvent) {
		if (RadialMenuViewer.activeMenu != null) return
		wheels.forEach { wheel ->
			if (event.matches(wheel.keyBinding, atLeast = true)) {
				class R(val action: HotkeyAction) : RadialMenuOption {
					override val isEnabled: Boolean
						get() = true

					override fun resolve() {
						action.execute()
					}

					override fun renderSlice(drawContext: GuiGraphicsExtractor) {
						drawContext.centeredText(MC.font, action.label, 0, 0, -1)
					}
				}
				RadialMenuViewer.activeMenu = object : RadialMenu {
					override val key: SavedKeyBinding
						get() = wheel.keyBinding
					override val options: List<RadialMenuOption> =
						wheel.options.map { R(it) }
				}
			}
		}
	}
}
