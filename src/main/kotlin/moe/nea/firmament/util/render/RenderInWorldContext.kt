/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
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
import net.minecraft.client.gl.VertexBuffer
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Camera
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.mixins.accessor.AccessorGameRenderer
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.MC

@RenderContextDSL
class RenderInWorldContext private constructor(
    private val tesselator: Tessellator,
    val matrixStack: MatrixStack,
    private val camera: Camera,
    private val tickDelta: Float,
    val vertexConsumers: VertexConsumerProvider.Immediate,
) {
    private val buffer = tesselator.buffer
    val effectiveFov = (MC.instance.gameRenderer as AccessorGameRenderer).getFov_firmament(camera, tickDelta, true)
    val effectiveFovScaleFactor = 1 / tan(toRadians(effectiveFov) / 2)

    fun color(color: me.shedaniel.math.Color) {
        color(color.red / 255F, color.green / 255f, color.blue / 255f, color.alpha / 255f)
    }

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

    fun withFacingThePlayer(position: Vec3d, block: FacingThePlayerContext.() -> Unit) {
        matrixStack.push()
        matrixStack.translate(position.x, position.y, position.z)
        val actualCameraDistance = position.distanceTo(camera.pos)
        val distanceToMoveTowardsCamera = if (actualCameraDistance < 10) 0.0 else -(actualCameraDistance - 10.0)
        val vec = position.subtract(camera.pos).multiply(distanceToMoveTowardsCamera / actualCameraDistance)
        matrixStack.translate(vec.x, vec.y, vec.z)
        matrixStack.multiply(camera.rotation)
        matrixStack.scale(-0.025F, -0.025F, -1F)

        FacingThePlayerContext(this).run(block)

        matrixStack.pop()
        vertexConsumers.drawCurrentLayer()
    }

    fun sprite(position: Vec3d, sprite: Sprite, width: Int, height: Int) {
        texture(
            position, sprite.atlasId, width, height, sprite.minU, sprite.minV, sprite.maxU, sprite.maxV
        )
    }

    fun texture(
        position: Vec3d, texture: Identifier, width: Int, height: Int,
        u1: Float, v1: Float,
        u2: Float, v2: Float,
    ) {
        withFacingThePlayer(position) {
            texture(texture, width, height, u1, v1, u2, v2)
        }
    }

    fun text(position: Vec3d, vararg texts: Text, verticalAlign: VerticalAlign = VerticalAlign.CENTER) {
        withFacingThePlayer(position) {
            text(*texts, verticalAlign = verticalAlign)
        }
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
        line(points.toList(), lineWidth)
    }

    fun tracer(toWhere: Vec3d, lineWidth: Float = 3f) {
        val cameraForward = Vector3f(0f, 0f, 1f).rotate(camera.rotation)
        line(camera.pos.add(Vec3d(cameraForward)), toWhere, lineWidth = lineWidth)
    }

    fun line(points: List<Vec3d>, lineWidth: Float = 10F) {
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram)
        RenderSystem.lineWidth(lineWidth)
        buffer.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)
        buffer.fixedColor(255, 255, 255, 255)

        val matrix = matrixStack.peek()
        var lastNormal: Vector3f? = null
        points.zipWithNext().forEach { (a, b) ->
            val normal = Vector3f(b.x.toFloat(), b.y.toFloat(), b.z.toFloat())
                .sub(a.x.toFloat(), a.y.toFloat(), a.z.toFloat())
                .normalize()
            val lastNormal0 = lastNormal ?: normal
            lastNormal = normal
            buffer.vertex(matrix.positionMatrix, a.x.toFloat(), a.y.toFloat(), a.z.toFloat())
                .normal(matrix, lastNormal0.x, lastNormal0.y, lastNormal0.z)
                .next()
            buffer.vertex(matrix.positionMatrix, b.x.toFloat(), b.y.toFloat(), b.z.toFloat())
                .normal(matrix, normal.x, normal.y, normal.z)
                .next()
        }
        buffer.unfixColor()

        tesselator.draw()
    }

    companion object {
        private fun doLine(
            matrix: MatrixStack.Entry,
            buf: BufferBuilder,
            i: Float,
            j: Float,
            k: Float,
            x: Float,
            y: Float,
            z: Float
        ) {
            val normal = Vector3f(x, y, z)
                .sub(i, j, k)
                .normalize()
            buf.vertex(matrix.positionMatrix, i, j, k)
                .normal(matrix, normal.x, normal.y, normal.z)
                .next()
            buf.vertex(matrix.positionMatrix, x, y, z)
                .normal(matrix, normal.x, normal.y, normal.z)
                .next()
        }


        private fun buildWireFrameCube(matrix: MatrixStack.Entry, buf: BufferBuilder) {
            buf.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)
            buf.fixedColor(255, 255, 255, 255)

            for (i in 0..1) {
                for (j in 0..1) {
                    val i = i.toFloat()
                    val j = j.toFloat()
                    doLine(matrix, buf, 0F, i, j, 1F, i, j)
                    doLine(matrix, buf, i, 0F, j, i, 1F, j)
                    doLine(matrix, buf, i, j, 0F, i, j, 1F)
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


