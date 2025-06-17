package moe.nea.firmament.util.render

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import io.github.notenoughupdates.moulconfig.platform.next
import java.util.OptionalInt
import org.joml.Matrix4f
import util.render.CustomRenderLayers
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.BufferAllocator
import net.minecraft.util.Identifier
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.collections.nonNegligibleSubSectionsAlignedWith
import moe.nea.firmament.util.math.Projections

object RenderCircleProgress {

	fun renderCircularSlice(
		drawContext: DrawContext,
		layer: RenderLayer,
		u1: Float,
		u2: Float,
		v1: Float,
		v2: Float,
		angleRadians: ClosedFloatingPointRange<Float>,
		color: Int = -1,
		innerCutoutRadius: Float = 0F
	) {
		drawContext.draw()
		val sections = angleRadians.nonNegligibleSubSectionsAlignedWith((τ / 8f).toFloat())
			.zipWithNext().toList()
		BufferAllocator(layer.vertexFormat.vertexSize * sections.size * 3).use { allocator ->

			val bufferBuilder = BufferBuilder(allocator, VertexFormat.DrawMode.TRIANGLES, layer.vertexFormat)
			val matrix: Matrix4f = drawContext.matrices.peek().positionMatrix

			for ((sectionStart, sectionEnd) in sections) {
				val firstPoint = Projections.Two.projectAngleOntoUnitBox(sectionStart.toDouble())
				val secondPoint = Projections.Two.projectAngleOntoUnitBox(sectionEnd.toDouble())
				fun ilerp(f: Float): Float =
					ilerp(-1f, 1f, f)

				bufferBuilder
					.vertex(matrix, secondPoint.x, secondPoint.y, 0F)
					.texture(lerp(u1, u2, ilerp(secondPoint.x)), lerp(v1, v2, ilerp(secondPoint.y)))
					.color(color)
					.next()
				bufferBuilder
					.vertex(matrix, firstPoint.x, firstPoint.y, 0F)
					.texture(lerp(u1, u2, ilerp(firstPoint.x)), lerp(v1, v2, ilerp(firstPoint.y)))
					.color(color)
					.next()
				bufferBuilder
					.vertex(matrix, 0F, 0F, 0F)
					.texture(lerp(u1, u2, ilerp(0F)), lerp(v1, v2, ilerp(0F)))
					.color(color)
					.next()
			}

			bufferBuilder.end().use { buffer ->
				// TODO: write a better utility to pass uniforms :sob: ill even take a mixin at this point
				if (innerCutoutRadius <= 0) {
					layer.draw(buffer)
					return
				}
				val vertexBuffer = layer.vertexFormat.uploadImmediateVertexBuffer(buffer.buffer)
				val indexBufferConstructor = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.TRIANGLES)
				val indexBuffer = indexBufferConstructor.getIndexBuffer(buffer.drawParameters.indexCount)
				RenderSystem.getDevice().createCommandEncoder().createRenderPass(
					MC.instance.framebuffer.colorAttachment,
					OptionalInt.empty(),
				).use { renderPass ->
					renderPass.setPipeline(layer.pipeline)
					renderPass.setUniform("InnerCutoutRadius", innerCutoutRadius)
					renderPass.setIndexBuffer(indexBuffer, indexBufferConstructor.indexType)
					renderPass.setVertexBuffer(0, vertexBuffer)
					renderPass.drawIndexed(0, buffer.drawParameters.indexCount)
				}
			}
		}
	}

	fun renderCircle(
		drawContext: DrawContext,
		texture: Identifier,
		progress: Float,
		u1: Float,
		u2: Float,
		v1: Float,
		v2: Float,
	) {
		renderCircularSlice(
			drawContext,
			CustomRenderLayers.GUI_TEXTURED_NO_DEPTH_TRIS.apply(texture),
			u1, u2, v1, v2,
			(-τ / 4).toFloat()..(progress * τ - τ / 4).toFloat()
		)
	}
}
