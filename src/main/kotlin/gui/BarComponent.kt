package moe.nea.firmament.gui

import io.github.notenoughupdates.moulconfig.common.MyResourceLocation
import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext
import io.github.notenoughupdates.moulconfig.observer.GetSetter
import io.github.notenoughupdates.moulconfig.platform.MoulConfigRenderContext
import me.shedaniel.math.Color
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.Identifier
import moe.nea.firmament.Firmament

class BarComponent(
	val progress: GetSetter<Double>, val total: GetSetter<Double>,
	val fillColor: Color,
	val emptyColor: Color,
) : GuiComponent() {
	override fun getWidth(): Int {
		return 80
	}

	override fun getHeight(): Int {
		return 8
	}

	data class Texture(
        val identifier: Identifier,
        val u1: Float, val v1: Float,
        val u2: Float, val v2: Float,
	) {
		fun draw(context: GuiGraphics, x: Int, y: Int, width: Int, height: Int, color: Color) {
			context.innerBlit(
				RenderPipelines.GUI_TEXTURED,
				identifier,
				x, y, x + width, x + height,
				u1, u2, v1, v2,
				color.color
			)
		}
	}

	companion object {
		val resource = Firmament.identifier("textures/gui/bar.png")
		val left = Texture(resource, 0 / 64F, 0 / 64F, 4 / 64F, 8 / 64F)
		val middle = Texture(resource, 4 / 64F, 0 / 64F, 8 / 64F, 8 / 64F)
		val right = Texture(resource, 8 / 64F, 0 / 64F, 12 / 64F, 8 / 64F)
		val segmentOverlay = Texture(resource, 12 / 64F, 0 / 64F, 15 / 64F, 8 / 64F)
	}

	private fun drawSection(
        context: GuiGraphics,
        texture: Texture,
        x: Int,
        y: Int,
        width: Int,
        sectionStart: Double,
        sectionEnd: Double
	) {
		if (sectionEnd < progress.get() && width == 4) {
			texture.draw(context, x, y, 4, 8, fillColor)
			return
		}
		if (sectionStart > progress.get() && width == 4) {
			texture.draw(context, x, y, 4, 8, emptyColor)
			return
		}
		val increasePerPixel = (sectionEnd - sectionStart) / width
		var valueAtPixel = sectionStart
		for (i in (0 until width)) {
			val newTex =
				Texture(texture.identifier, texture.u1 + i / 64F, texture.v1, texture.u1 + (i + 1) / 64F, texture.v2)
			newTex.draw(
				context, x + i, y, 1, 8,
				if (valueAtPixel < progress.get()) fillColor else emptyColor
			)
			valueAtPixel += increasePerPixel
		}
	}

	override fun render(context: GuiImmediateContext) {
		val renderContext = (context.renderContext as MoulConfigRenderContext).drawContext
		var i = 0
		val x = 0
		val y = 0
		while (i < context.width - 4) {
			drawSection(
				renderContext,
				if (i == 0) left else middle,
				x + i, y,
				(context.width - (i + 4)).coerceAtMost(4),
				i * total.get() / context.width, (i + 4) * total.get() / context.width
			)
			i += 4
		}
		drawSection(
			renderContext,
			right,
			x + context.width - 4,
			y,
			4,
			(context.width - 4) * total.get() / context.width,
			total.get()
		)

	}

}

fun Identifier.toMoulConfig(): MyResourceLocation {
	return MyResourceLocation(this.namespace, this.path)
}
