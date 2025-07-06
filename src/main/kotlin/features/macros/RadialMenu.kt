package moe.nea.firmament.features.macros

import org.joml.Vector2f
import util.render.CustomRenderLayers
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import net.minecraft.client.gui.DrawContext
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HudRenderEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.events.WorldKeyboardEvent
import moe.nea.firmament.events.WorldMouseMoveEvent
import moe.nea.firmament.features.macros.RadialMenuViewer.RadialMenu
import moe.nea.firmament.features.macros.RadialMenuViewer.RadialMenuOption
import moe.nea.firmament.keybindings.SavedKeyBinding
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.render.RenderCircleProgress
import moe.nea.firmament.util.render.drawLine
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
		fun renderSlice(drawContext: DrawContext)
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
		val mat = event.context.matrices
		mat.push()
		mat.translate(
			(MC.window.scaledWidth) / 2F,
			(MC.window.scaledHeight) / 2F,
			0F
		)
		val sliceWidth = (τ / menu.options.size).toFloat()
		var selectedAngle = wrapAngle(atan2(delta.y, delta.x))
		if (delta.lengthSquared() < INNER_CIRCLE_RADIUS * INNER_CIRCLE_RADIUS)
			selectedAngle = Float.NaN
		for ((idx, option) in menu.options.withIndex()) {
			val range = (sliceWidth * idx)..(sliceWidth * (idx + 1))
			mat.push()
			mat.scale(64F, 64F, 1F)
			val cutout = INNER_CIRCLE_RADIUS / 64F / 2
			RenderCircleProgress.renderCircularSlice(
				event.context,
				CustomRenderLayers.TRANSLUCENT_CIRCLE_GUI,
				0F, 1F, 0F, 1F,
				range,
				color = if (selectedAngle in range) 0x70A0A0A0 else 0x70FFFFFF,
				innerCutoutRadius = cutout
			)
			mat.pop()
			mat.push()
			val centreAngle = lerpAngle(range.start, range.endInclusive, 0.5F)
			val vec = Vector2f(cos(centreAngle), sin(centreAngle)).mul(40F)
			mat.translate(vec.x, vec.y, 0F)
			option.renderSlice(event.context)
			mat.pop()
		}
		event.context.drawLine(1, 1, delta.x.toInt(), delta.y.toInt(), me.shedaniel.math.Color.ofOpaque(0x00FF00))
		mat.pop()
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
	var wheels = MacroData.DConfig.data.wheels
		private set

	fun setWheels(wheels: List<MacroWheel>) {
		this.wheels = wheels
		RadialMenuViewer.activeMenu = null
	}

	@Subscribe
	fun onOpen(event: WorldKeyboardEvent) {
		if (RadialMenuViewer.activeMenu != null) return
		wheels.forEach { wheel ->
			if (event.matches(wheel.key, atLeast = true)) {
				class R(val action: HotkeyAction) : RadialMenuOption {
					override val isEnabled: Boolean
						get() = true

					override fun resolve() {
						action.execute()
					}

					override fun renderSlice(drawContext: DrawContext) {
						drawContext.drawCenteredTextWithShadow(MC.font, action.label, 0, 0, -1)
					}
				}
				RadialMenuViewer.activeMenu = object : RadialMenu {
					override val key: SavedKeyBinding
						get() = wheel.key
					override val options: List<RadialMenuOption> =
						wheel.options.map { R(it) }
				}
			}
		}
	}
}
