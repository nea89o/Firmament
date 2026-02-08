
package moe.nea.firmament.util.render

import org.joml.Matrix4f
import util.render.CustomRenderLayers
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.LightTexture
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.core.BlockPos
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.assertTrueOr

@RenderContextDSL
class FacingThePlayerContext(val worldContext: RenderInWorldContext) {
    val matrixStack by worldContext::matrixStack
    fun waypoint(position: BlockPos, label: Component) {
        text(
            label,
            Component.literal("§e${FirmFormatters.formatDistance(MC.player?.position?.distanceTo(position.center) ?: 42069.0)}")
        )
    }

    fun text(
		vararg texts: Component,
		verticalAlign: RenderInWorldContext.VerticalAlign = RenderInWorldContext.VerticalAlign.CENTER,
		background: Int = 0x70808080,
    ) {
        assertTrueOr(texts.isNotEmpty()) { return@text }
        for ((index, text) in texts.withIndex()) {
            worldContext.matrixStack.pushPose()
            val width = MC.font.width(text)
            worldContext.matrixStack.translate(-width / 2F, verticalAlign.align(index, texts.size), 0F)
            val vertexConsumer: VertexConsumer =
                worldContext.vertexConsumers.getBuffer(RenderTypes.textBackgroundSeeThrough())
            val matrix4f = worldContext.matrixStack.last().pose()
            vertexConsumer.addVertex(matrix4f, -1.0f, -1.0f, 0.0f).setColor(background)
                .setLight(LightTexture.FULL_BLOCK)
            vertexConsumer.addVertex(matrix4f, -1.0f, MC.font.lineHeight.toFloat(), 0.0f).setColor(background)
                .setLight(LightTexture.FULL_BLOCK)
            vertexConsumer.addVertex(matrix4f, width.toFloat(), MC.font.lineHeight.toFloat(), 0.0f)
                .setColor(background)
                .setLight(LightTexture.FULL_BLOCK)
            vertexConsumer.addVertex(matrix4f, width.toFloat(), -1.0f, 0.0f).setColor(background)
                .setLight(LightTexture.FULL_BLOCK)
            worldContext.matrixStack.translate(0F, 0F, 0.01F)

            MC.font.drawInBatch(
                text,
                0F,
                0F,
                -1,
                false,
                worldContext.matrixStack.last().pose(),
                worldContext.vertexConsumers,
                Font.DisplayMode.SEE_THROUGH,
                0,
                LightTexture.FULL_BRIGHT
            )
            worldContext.matrixStack.popPose()
        }
    }


    fun texture(
		texture: Identifier, width: Int, height: Int,
		u1: Float, v1: Float,
		u2: Float, v2: Float,
    ) {
		val buf = worldContext.vertexConsumers.getBuffer(CustomRenderLayers.GUI_TEXTURED_NO_DEPTH_TRIS.apply(texture)) // TODO: this is strictly an incorrect render layer
        val hw = width / 2F
        val hh = height / 2F
        val matrix4f: Matrix4f = worldContext.matrixStack.last().pose()
        buf.addVertex(matrix4f, -hw, -hh, 0F)
            .setColor(-1)
            .setUv(u1, v1)
        buf.addVertex(matrix4f, -hw, +hh, 0F)
            .setColor(-1)
            .setUv(u1, v2)
        buf.addVertex(matrix4f, +hw, +hh, 0F)
            .setColor(-1)
            .setUv(u2, v2)
        buf.addVertex(matrix4f, +hw, -hh, 0F)
            .setColor(-1)
            .setUv(u2, v1)
	    worldContext.vertexConsumers.endBatch()
    }

}
