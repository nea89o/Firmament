package moe.nea.firmament.util

import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent
import me.shedaniel.math.Point
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

class MoulConfigFragment(
	context: GuiContext,
	val position: Point,
	val dismiss: () -> Unit
) : MoulConfigScreenComponent(Component.empty(), context, null) {
	init {
		this.init(MC.screen!!.width, MC.screen!!.height)
	}

	override fun createContext(drawContext: GuiGraphics?): GuiImmediateContext {
		val oldContext = super.createContext(drawContext)
		return oldContext.translated(
			position.x,
			position.y,
			guiContext.root.width,
			guiContext.root.height,
		)
	}


	override fun render(drawContext: GuiGraphics, i: Int, j: Int, f: Float) {
		val ctx = createContext(drawContext)
		val m = drawContext.pose()
		m.pushMatrix()
		m.translate(position.x.toFloat(), position.y.toFloat())
		guiContext.root.render(ctx)
		m.popMatrix()
		ctx.renderContext.renderExtraLayers()
	}

	override fun onClose() {
		dismiss()
	}
}
