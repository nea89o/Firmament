/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package moe.nea.firmament.util.render

import com.mojang.blaze3d.systems.RenderSystem
import java.lang.Math.pow
import org.joml.Matrix4f
import org.joml.Vector3f
import net.minecraft.client.gl.VertexBuffer
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Camera
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.MatrixStack.Entry
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class RenderInWorldContext private constructor(
    private val tesselator: Tessellator,
    private val matrixStack: MatrixStack,
    private val camera: Camera
) {
    private val buffer = tesselator.buffer

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
        private fun doLine(matrix: Entry, buf: BufferBuilder, i: Number, j: Number, k: Number, x: Number, y: Number, z: Number) {
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

        fun renderInWorld(matrices: MatrixStack, camera: Camera, block: RenderInWorldContext. () -> Unit) {
            RenderSystem.disableDepthTest()
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()
            RenderSystem.disableCull()

            matrices.push()
            matrices.translate(-camera.pos.x, -camera.pos.y, -camera.pos.z)

            val ctx = RenderInWorldContext(RenderSystem.renderThreadTesselator(), matrices, camera)

            block(ctx)

            matrices.pop()

            RenderSystem.setShaderColor(1F, 1F, 1F, 1F)
            VertexBuffer.unbind()
            RenderSystem.enableDepthTest()
            RenderSystem.enableCull()
            RenderSystem.disableBlend()
        }
    }
}


