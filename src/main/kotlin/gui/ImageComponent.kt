package moe.nea.firmament.gui

import io.github.notenoughupdates.moulconfig.common.MyResourceLocation
import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext
import java.util.function.Supplier

class ImageComponent(
	private val width: Int,
	private val height: Int,
	val resourceLocation: Supplier<MyResourceLocation>,
	val u1: Float,
	val u2: Float,
	val v1: Float,
	val v2: Float,
) : GuiComponent() {
	override fun getWidth(): Int {
		return width
	}

	override fun getHeight(): Int {
		return height
	}

	override fun render(context: GuiImmediateContext) {
		context.renderContext.drawComplexTexture(
			resourceLocation.get(),
			0f, 0f,
			context.width.toFloat(), context.height.toFloat(),
			{
				it.uv(u1, v1, u2, v2)
			}
		)
	}
}
