
package moe.nea.firmament.util.render

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.textures.FilterMode
import org.joml.Matrix4f
import net.minecraft.client.gui.Font
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.gui.font.TextRenderable
import net.minecraft.client.gui.render.TextureSetup
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.core.BlockPos
import net.minecraft.util.CommonColors
import net.minecraft.util.LightCoordsUtil
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.assertTrueOr
import moe.nea.firmament.util.center

@RenderContextDSL
class FacingThePlayerContext(val worldContext: RenderInWorldContext) {
    val matrixStack by worldContext::matrixStack
    fun waypoint(position: BlockPos, label: Component) {
        text(
            label,
            Component.literal("§e${FirmFormatters.formatDistance(MC.player?.position()?.distanceTo(position.center()) ?: 42069.0)}")
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
                CustomRenderer.getBuffer(RenderPipelines.TEXT_BACKGROUND_SEE_THROUGH)
            val matrix4f = worldContext.matrixStack.last().pose()
            vertexConsumer.addVertex(matrix4f, -1.0f, -1.0f, 0.0f).setColor(background)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
            vertexConsumer.addVertex(matrix4f, -1.0f, MC.font.lineHeight.toFloat(), 0.0f).setColor(background)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
            vertexConsumer.addVertex(matrix4f, width.toFloat(), MC.font.lineHeight.toFloat(), 0.0f)
                .setColor(background)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
            vertexConsumer.addVertex(matrix4f, width.toFloat(), -1.0f, 0.0f).setColor(background)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
            worldContext.matrixStack.translate(0F, 0F, 0.01F)

			val prepared = MC.font.prepareText(text.visualOrderText, 0f, 0f, CommonColors.WHITE, false, false, 0);
            prepared.visit(object : Font.GlyphVisitor {
				override fun acceptRenderable(glyph : TextRenderable) {
					val setup = TextureSetup.singleTextureWithLightmap(glyph.textureView(), RenderSystem.getSamplerCache().getClampToEdge(
						FilterMode.NEAREST));
					// will probably not work with caxton but whatever at least it works with TTF font resource packs
					val buffer = CustomRenderer.getBuffer(if (glyph.guiPipeline() == RenderPipelines.GUI_TEXT_GRAYSCALE) RenderPipelines.TEXT_GRAYSCALE_SEE_THROUGH else RenderPipelines.TEXT_SEE_THROUGH, setup);

					glyph.render(matrix4f, buffer, LightCoordsUtil.FULL_BRIGHT, false);
				}
			});
            worldContext.matrixStack.popPose()
        }
    }


    fun texture(
		texture: Identifier, width: Int, height: Int,
		u1: Float, v1: Float,
		u2: Float, v2: Float,
    ) {
		val tex = MC.textureManager.getTexture(texture);
		val buf = CustomRenderer.getBuffer(CustomRenderPipelines.GUI_TEXTURED_NO_DEPTH_TRIANGLES, TextureSetup.singleTexture(tex.textureView, tex.sampler)) // TODO: this is strictly an incorrect render layer
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
    }

}
