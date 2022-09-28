package moe.nea.notenoughupdates.util.render

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gl.VertexBuffer
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Camera
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class RenderBlockContext(val tesselator: Tessellator, val camPos: Vec3d) {
    val buffer = tesselator.buffer
    fun color(red: Float, green: Float, blue: Float, alpha: Float) {
        RenderSystem.setShaderColor(red, green, blue, alpha)
    }

    fun block(blockPos: BlockPos) {
        val matrixStack = RenderSystem.getModelViewStack()
        matrixStack.push()
        matrixStack.translate(blockPos.x - camPos.x, blockPos.y - camPos.y, blockPos.z - camPos.z)
        RenderSystem.applyModelViewMatrix()
        RenderSystem.setShader(GameRenderer::getPositionColorShader)
        buildCube(buffer)
        tesselator.draw()
        matrixStack.pop()
    }

    companion object {
        fun buildCube(buf: BufferBuilder) {
            buf.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR)
            buf.fixedColor(255, 255, 255, 255)
            buf.vertex(0.0, 0.0, 0.0).next()
            buf.vertex(0.0, 0.0, 1.0).next()
            buf.vertex(0.0, 1.0, 1.0).next()
            buf.vertex(1.0, 1.0, 0.0).next()
            buf.vertex(0.0, 0.0, 0.0).next()
            buf.vertex(0.0, 1.0, 0.0).next()
            buf.vertex(1.0, 0.0, 1.0).next()
            buf.vertex(0.0, 0.0, 0.0).next()
            buf.vertex(1.0, 0.0, 0.0).next()
            buf.vertex(1.0, 1.0, 0.0).next()
            buf.vertex(1.0, 0.0, 0.0).next()
            buf.vertex(0.0, 0.0, 0.0).next()
            buf.vertex(0.0, 0.0, 0.0).next()
            buf.vertex(0.0, 1.0, 1.0).next()
            buf.vertex(0.0, 1.0, 0.0).next()
            buf.vertex(1.0, 0.0, 1.0).next()
            buf.vertex(0.0, 0.0, 1.0).next()
            buf.vertex(0.0, 0.0, 0.0).next()
            buf.vertex(0.0, 1.0, 1.0).next()
            buf.vertex(0.0, 0.0, 1.0).next()
            buf.vertex(1.0, 0.0, 1.0).next()
            buf.vertex(1.0, 1.0, 1.0).next()
            buf.vertex(1.0, 0.0, 0.0).next()
            buf.vertex(1.0, 1.0, 0.0).next()
            buf.vertex(1.0, 0.0, 0.0).next()
            buf.vertex(1.0, 1.0, 1.0).next()
            buf.vertex(1.0, 0.0, 1.0).next()
            buf.vertex(1.0, 1.0, 1.0).next()
            buf.vertex(1.0, 1.0, 0.0).next()
            buf.vertex(0.0, 1.0, 0.0).next()
            buf.vertex(1.0, 1.0, 1.0).next()
            buf.vertex(0.0, 1.0, 0.0).next()
            buf.vertex(0.0, 1.0, 1.0).next()
            buf.vertex(1.0, 1.0, 1.0).next()
            buf.vertex(0.0, 1.0, 1.0).next()
            buf.vertex(1.0, 0.0, 1.0).next()
            buf.unfixColor()
        }

        fun renderBlocks(camera: Camera, block: RenderBlockContext. () -> Unit) {
            RenderSystem.disableDepthTest()
            RenderSystem.disableTexture()
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()

            val ctx = RenderBlockContext(Tessellator.getInstance(), camera.pos)
            block(ctx)

            VertexBuffer.unbind()
            RenderSystem.enableDepthTest()
            RenderSystem.enableTexture()
            RenderSystem.disableBlend()
        }
    }
}


