package moe.nea.firmament.util.render

import com.mojang.blaze3d.systems.RenderSystem
import me.shedaniel.math.Color
import org.joml.Matrix4f
import util.render.CustomRenderLayers
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.util.Identifier
import moe.nea.firmament.util.MC

fun DrawContext.isUntranslatedGuiDrawContext(): Boolean {
	return (matrices.peek().positionMatrix.properties() and Matrix4f.PROPERTY_IDENTITY.toInt()) != 0
}

@Deprecated("Use the other drawGuiTexture")
fun DrawContext.drawGuiTexture(
	x: Int, y: Int, z: Int, width: Int, height: Int, sprite: Identifier
) = this.drawGuiTexture(RenderLayer::getGuiTextured, sprite, x, y, width, height)

fun DrawContext.drawGuiTexture(
	sprite: Identifier,
	x: Int, y: Int, width: Int, height: Int
) = this.drawGuiTexture(RenderLayer::getGuiTextured, sprite, x, y, width, height)

fun DrawContext.drawTexture(
	sprite: Identifier,
	x: Int,
	y: Int,
	u: Float,
	v: Float,
	width: Int,
	height: Int,
	textureWidth: Int,
	textureHeight: Int
) {
	this.drawTexture(RenderLayer::getGuiTextured,
	                 sprite,
	                 x,
	                 y,
	                 u,
	                 v,
	                 width,
	                 height,
	                 width,
	                 height,
	                 textureWidth,
	                 textureHeight)
}

fun DrawContext.drawLine(fromX: Int, fromY: Int, toX: Int, toY: Int, color: Color) {
	// TODO: push scissors
	// TODO: use matrix translations and a different render layer
	if (toY < fromY) {
		drawLine(toX, toY, fromX, fromY, color)
		return
	}
	RenderSystem.lineWidth(MC.window.scaleFactor.toFloat())
	draw { vertexConsumers ->
		val buf = vertexConsumers.getBuffer(CustomRenderLayers.LINES)
		val matrix = this.matrices.peek()
		buf.vertex(matrix, fromX.toFloat(), fromY.toFloat(), 0F).color(color.color)
			.normal(toX - fromX.toFloat(), toY - fromY.toFloat(), 0F)
		buf.vertex(matrix, toX.toFloat(), toY.toFloat(), 0F).color(color.color)
			.normal(toX - fromX.toFloat(), toY - fromY.toFloat(), 0F)
	}
}

