/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util.render

import com.mojang.blaze3d.systems.RenderSystem
import java.lang.Math.pow
import java.lang.Math.toRadians
import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.math.tan
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gl.VertexBuffer
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Camera
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.MatrixStack.Entry
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.mixins.accessor.AccessorGameRenderer
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.assertTrueOr

class RenderInWorldContext private constructor(
    private val tesselator: Tessellator,
    private val matrixStack: MatrixStack,
    private val camera: Camera,
    private val tickDelta: Float,
    private val vertexConsumers: VertexConsumerProvider.Immediate,
) {
    private val buffer = tesselator.buffer
    val effectiveFov = (MC.instance.gameRenderer as AccessorGameRenderer).getFov_firmament(camera, tickDelta, true)
    val effectiveFovScaleFactor = 1 / tan(toRadians(effectiveFov) / 2)

    fun color(red: Float, green: Float, blue: Float, alpha: Float) {
        RenderSystem.setShaderColor(red, green, blue, alpha)
    }

    fun block(blockPos: BlockPos) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram)
        matrixStack.push()
        matrixStack.translate(blockPos.x.toFloat(), blockPos.y.toFloat(), blockPos.z.toFloat())
        buildCube(matrixStack.peek().positionMatrix, buffer)
        tesselator.draw()
        matrixStack.pop()
    }

    enum class VerticalAlign {
        TOP, BOTTOM, CENTER;

        fun align(index: Int, count: Int): Float {
            return when (this) {
                CENTER -> (index - count / 2F) * (1 + MC.font.fontHeight.toFloat())
                BOTTOM -> (index - count) * (1 + MC.font.fontHeight.toFloat())
                TOP -> (index) * (1 + MC.font.fontHeight.toFloat())
            }
        }
    }

    fun waypoint(position: BlockPos, label: Text) {
        text(
            position.toCenterPos(),
            label,
            Text.literal("§e${FirmFormatters.formatDistance(MC.player?.pos?.distanceTo(position.toCenterPos()) ?: 42069.0)}")
        )
    }

    fun text(position: Vec3d, vararg texts: Text, verticalAlign: VerticalAlign = VerticalAlign.CENTER) {
        assertTrueOr(texts.isNotEmpty()) { return@text }
        matrixStack.push()
        matrixStack.translate(position.x, position.y, position.z)
        val actualCameraDistance = position.distanceTo(camera.pos)
        val distanceToMoveTowardsCamera = if (actualCameraDistance < 10) 0.0 else -(actualCameraDistance - 10.0)
        val vec = position.subtract(camera.pos).multiply(distanceToMoveTowardsCamera / actualCameraDistance)
        matrixStack.translate(vec.x, vec.y, vec.z)
        matrixStack.multiply(camera.rotation)
        matrixStack.scale(-0.025F, -0.025F, -1F)

        for ((index, text) in texts.withIndex()) {
            matrixStack.push()
            val width = MC.font.getWidth(text)
            matrixStack.translate(-width / 2F, verticalAlign.align(index, texts.size), 0F)
            val vertexConsumer: VertexConsumer = vertexConsumers.getBuffer(RenderLayer.getTextBackgroundSeeThrough())
            val matrix4f = matrixStack.peek().positionMatrix
            vertexConsumer.vertex(matrix4f, -1.0f, -1.0f, 0.0f).color(0x70808080)
                .light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE).next()
            vertexConsumer.vertex(matrix4f, -1.0f, MC.font.fontHeight.toFloat(), 0.0f).color(0x70808080)
                .light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE).next()
            vertexConsumer.vertex(matrix4f, width.toFloat(), MC.font.fontHeight.toFloat(), 0.0f)
                .color(0x70808080)
                .light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE).next()
            vertexConsumer.vertex(matrix4f, width.toFloat(), -1.0f, 0.0f).color(0x70808080)
                .light(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE).next()
            matrixStack.translate(0F, 0F, 0.01F)

            MC.font.draw(
                text,
                0F,
                0F,
                -1,
                false,
                matrixStack.peek().positionMatrix,
                vertexConsumers,
                TextRenderer.TextLayerType.SEE_THROUGH,
                0,
                LightmapTextureManager.MAX_LIGHT_COORDINATE
            )
            matrixStack.pop()
        }
        matrixStack.pop()
        vertexConsumers.drawCurrentLayer()
    }

    fun tinyBlock(vec3d: Vec3d, size: Float) {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram)
        matrixStack.push()
        matrixStack.translate(vec3d.x, vec3d.y, vec3d.z)
        matrixStack.scale(size, size, size)
        matrixStack.translate(-.5, -.5, -.5)
        buildCube(matrixStack.peek().positionMatrix, buffer)
        tesselator.draw()
        matrixStack.pop()
    }

    fun wireframeCube(blockPos: BlockPos, lineWidth: Float = 10F) {
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram)
        matrixStack.push()
        RenderSystem.lineWidth(lineWidth / pow(camera.pos.squaredDistanceTo(blockPos.toCenterPos()), 0.25).toFloat())
        matrixStack.translate(blockPos.x.toFloat(), blockPos.y.toFloat(), blockPos.z.toFloat())
        buildWireFrameCube(matrixStack.peek(), buffer)
        tesselator.draw()
        matrixStack.pop()
    }

    fun line(vararg points: Vec3d, lineWidth: Float = 10F) {
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram)
        RenderSystem.lineWidth(lineWidth / pow(camera.pos.squaredDistanceTo(points.first()), 0.25).toFloat())
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)
        buffer.fixedColor(255, 255, 255, 255)

        points.toList().zipWithNext().forEach { (a, b) ->
            doLine(matrixStack.peek(), buffer, a.x, a.y, a.z, b.x, b.y, b.z)
        }
        buffer.unfixColor()

        tesselator.draw()
    }

    companion object {
        private fun doLine(
            matrix: Entry,
            buf: BufferBuilder,
            i: Number,
            j: Number,
            k: Number,
            x: Number,
            y: Number,
            z: Number
        ) {
            val normal = Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
                .sub(i.toFloat(), j.toFloat(), k.toFloat())
                .mul(-1F)
            buf.vertex(matrix.positionMatrix, i.toFloat(), j.toFloat(), k.toFloat())
                .normal(matrix.normalMatrix, normal.x, normal.y, normal.z).next()
            buf.vertex(matrix.positionMatrix, x.toFloat(), y.toFloat(), z.toFloat())
                .normal(matrix.normalMatrix, normal.x, normal.y, normal.z).next()
        }


        private fun buildWireFrameCube(matrix: MatrixStack.Entry, buf: BufferBuilder) {
            buf.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)
            buf.fixedColor(255, 255, 255, 255)

            for (i in 0..1) {
                for (j in 0..1) {
                    doLine(matrix, buf, 0, i, j, 1, i, j)
                    doLine(matrix, buf, i, 0, j, i, 1, j)
                    doLine(matrix, buf, i, j, 0, i, j, 1)
                }
            }
            buf.unfixColor()
        }

        private fun buildCube(matrix: Matrix4f, buf: BufferBuilder) {
            buf.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR)
            buf.fixedColor(255, 255, 255, 255)
            buf.vertex(matrix, 0.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 1.0F).next()
            buf.unfixColor()
        }


        fun renderInWorld(event: WorldRenderLastEvent, block: RenderInWorldContext. () -> Unit) {
            RenderSystem.disableDepthTest()
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()
            RenderSystem.disableCull()

            event.matrices.push()
            event.matrices.translate(-event.camera.pos.x, -event.camera.pos.y, -event.camera.pos.z)

            val ctx = RenderInWorldContext(
                RenderSystem.renderThreadTesselator(),
                event.matrices,
                event.camera,
                event.tickDelta,
                event.vertexConsumers
            )

            block(ctx)

            event.matrices.pop()

            RenderSystem.setShaderColor(1F, 1F, 1F, 1F)
            VertexBuffer.unbind()
            RenderSystem.enableDepthTest()
            RenderSystem.enableCull()
            RenderSystem.disableBlend()
        }
    }
}


