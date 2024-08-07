/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util.render

import com.mojang.blaze3d.systems.RenderSystem
import io.github.notenoughupdates.moulconfig.platform.next
import org.joml.Matrix4f
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.assertTrueOr

@RenderContextDSL
class FacingThePlayerContext(val worldContext: RenderInWorldContext) {
    val matrixStack by worldContext::matrixStack
    fun waypoint(position: BlockPos, label: Text) {
        text(
            label,
            Text.literal("§e${FirmFormatters.formatDistance(MC.player?.pos?.distanceTo(position.toCenterPos()) ?: 42069.0)}")
        )
    }

    fun text(
        vararg texts: Text,
        verticalAlign: RenderInWorldContext.VerticalAlign = RenderInWorldContext.VerticalAlign.CENTER,
        background: Int = 0x70808080,
    ) {
        assertTrueOr(texts.isNotEmpty()) { return@text }
        for ((index, text) in texts.withIndex()) {
            worldContext.matrixStack.push()
            val width = MC.font.getWidth(text)
            worldContext.matrixStack.translate(-width / 2F, verticalAlign.align(index, texts.size), 0F)
            val vertexConsumer: VertexConsumer =
                worldContext.vertexConsumers.getBuffer(RenderLayer.getTextBackgroundSeeThrough())
            val matrix4f = worldContext.matrixStack.peek().positionMatrix
            vertexConsumer.vertex(matrix4f, -1.0f, -1.0f, 0.0f).color(background)
                .light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE).next()
            vertexConsumer.vertex(matrix4f, -1.0f, MC.font.fontHeight.toFloat(), 0.0f).color(background)
                .light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE).next()
            vertexConsumer.vertex(matrix4f, width.toFloat(), MC.font.fontHeight.toFloat(), 0.0f)
                .color(background)
                .light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE).next()
            vertexConsumer.vertex(matrix4f, width.toFloat(), -1.0f, 0.0f).color(background)
                .light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE).next()
            worldContext.matrixStack.translate(0F, 0F, 0.01F)

            MC.font.draw(
                text,
                0F,
                0F,
                -1,
                false,
                worldContext.matrixStack.peek().positionMatrix,
                worldContext.vertexConsumers,
                TextRenderer.TextLayerType.SEE_THROUGH,
                0,
                LightmapTextureManager.MAX_LIGHT_COORDINATE
            )
            worldContext.matrixStack.pop()
        }
    }


    fun texture(
        texture: Identifier, width: Int, height: Int,
        u1: Float, v1: Float,
        u2: Float, v2: Float,
    ) {
        RenderSystem.setShaderTexture(0, texture)
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram)
        val hw = width / 2F
        val hh = height / 2F
        val matrix4f: Matrix4f = worldContext.matrixStack.peek().positionMatrix
        val buf = Tessellator.getInstance()
            .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR)
        buf.vertex(matrix4f, -hw, -hh, 0F)
            .color(-1)
            .texture(u1, v1).next()
        buf.vertex(matrix4f, -hw, +hh, 0F)
            .color(-1)
            .texture(u1, v2).next()
        buf.vertex(matrix4f, +hw, +hh, 0F)
            .color(-1)
            .texture(u2, v2).next()
        buf.vertex(matrix4f, +hw, -hh, 0F)
            .color(-1)
            .texture(u2, v1).next()
        BufferRenderer.drawWithGlobalProgram(buf.end())
    }

}
