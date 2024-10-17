package moe.nea.firmament.util.render

import com.mojang.blaze3d.systems.RenderSystem
import me.shedaniel.math.Color
import org.joml.Matrix4f
import net.minecraft.client.gui.DrawContext
import moe.nea.firmament.util.MC

fun DrawContext.isUntranslatedGuiDrawContext(): Boolean {
	return (matrices.peek().positionMatrix.properties() and Matrix4f.PROPERTY_IDENTITY.toInt()) != 0
}

fun DrawContext.drawLine(fromX: Int, fromY: Int, toX: Int, toY: Int, color: Color) {
	// TODO: push scissors
	if (toY < fromY) {
		drawLine(toX, toY, fromX, fromY, color)
		return
	}
	RenderSystem.lineWidth(MC.window.scaleFactor.toFloat())
	val buf = this.vertexConsumers.getBuffer(RenderInWorldContext.RenderLayers.LINES)
	buf.vertex(fromX.toFloat(), fromY.toFloat(), 0F).color(color.color)
		.normal(toX - fromX.toFloat(), toY - fromY.toFloat(), 0F)
	buf.vertex(toX.toFloat(), toY.toFloat(), 0F).color(color.color)
		.normal(toX - fromX.toFloat(), toY - fromY.toFloat(), 0F)
	this.draw()
}

