package moe.nea.firmament.util.render

import com.mojang.blaze3d.systems.RenderSystem
import me.shedaniel.math.Color
import org.joml.Matrix4f
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormat.DrawMode
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.Identifier
import net.minecraft.util.TriState
import net.minecraft.util.Util
import moe.nea.firmament.util.MC

fun DrawContext.isUntranslatedGuiDrawContext(): Boolean {
	return (matrices.peek().positionMatrix.properties() and Matrix4f.PROPERTY_IDENTITY.toInt()) != 0
}

object GuiRenderLayers {
	val GUI_TEXTURED_NO_DEPTH = Util.memoize<Identifier, RenderLayer> { texture: Identifier ->
		RenderLayer.of("firmament_gui_textured_no_depth",
		               VertexFormats.POSITION_TEXTURE_COLOR,
		               DrawMode.QUADS,
		               DEFAULT_BUFFER_SIZE,
		               MultiPhaseParameters.builder()
			               .texture(RenderPhase.Texture(texture, TriState.FALSE, false))
			               .program(RenderPhase.POSITION_TEXTURE_COLOR_PROGRAM)
			               .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
			               .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
			               .build(false))
	}
	val GUI_TEXTURED_TRIS = Util.memoize { texture: Identifier ->
		RenderLayer.of("firmament_gui_textured_overlay_tris",
		               VertexFormats.POSITION_TEXTURE_COLOR,
		               DrawMode.TRIANGLES,
		               DEFAULT_BUFFER_SIZE,
		               MultiPhaseParameters.builder()
			               .texture(RenderPhase.Texture(texture, TriState.DEFAULT, false))
			               .program(RenderPhase.POSITION_TEXTURE_COLOR_PROGRAM)
			               .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
			               .depthTest(RenderPhase.ALWAYS_DEPTH_TEST)
			               .writeMaskState(RenderPhase.COLOR_MASK)
			               .build(false))
	}
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
		val buf = vertexConsumers.getBuffer(RenderInWorldContext.RenderLayers.LINES)
		buf.vertex(fromX.toFloat(), fromY.toFloat(), 0F).color(color.color)
			.normal(toX - fromX.toFloat(), toY - fromY.toFloat(), 0F)
		buf.vertex(toX.toFloat(), toY.toFloat(), 0F).color(color.color)
			.normal(toX - fromX.toFloat(), toY - fromY.toFloat(), 0F)
	}
}

