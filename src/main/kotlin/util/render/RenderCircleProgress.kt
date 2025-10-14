package moe.nea.firmament.util.render

import com.mojang.blaze3d.vertex.VertexFormat
import util.render.CustomRenderLayers
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.ScreenRect
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.BufferAllocator
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.collections.nonNegligibleSubSectionsAlignedWith
import moe.nea.firmament.util.math.Projections
import moe.nea.firmament.util.mc.CustomRenderPassHelper

object RenderCircleProgress {


	data class State(
		override val x1: Int,
		override val x2: Int,
		override val y1: Int,
		override val y2: Int,
		val layer: RenderLayer.MultiPhase,
		val u1: Float,
		val u2: Float,
		val v1: Float,
		val v2: Float,
		val angleRadians: ClosedFloatingPointRange<Float>,
		val color: Int,
		val innerCutoutRadius: Float,
		override val scale: Float,
		override val bounds: ScreenRect?,
		override val scissorArea: ScreenRect?,
	) : MultiSpecialGuiRenderState() {
		override fun createRenderer(vertexConsumers: VertexConsumerProvider.Immediate): MultiSpecialGuiRenderer<out MultiSpecialGuiRenderState> {
			return Renderer(vertexConsumers)
		}
	}

	class Renderer(vertexConsumers: VertexConsumerProvider.Immediate) :
		MultiSpecialGuiRenderer<State>(vertexConsumers) {
		override fun render(
			state: State,
			matrices: MatrixStack
		) {
			matrices.push()
			matrices.translate(0F, -1F, 0F)
			val sections = state.angleRadians.nonNegligibleSubSectionsAlignedWith((τ / 8f).toFloat())
				.zipWithNext().toList()
			val u1 = state.u1
			val u2 = state.u2
			val v1 = state.v1
			val v2 = state.v2
			val color = state.color
			val matrix = matrices.peek().positionMatrix
			BufferAllocator(state.layer.vertexFormat.vertexSize * sections.size * 3).use { allocator ->

				val bufferBuilder = BufferBuilder(allocator, VertexFormat.DrawMode.TRIANGLES, state.layer.vertexFormat)

				for ((sectionStart, sectionEnd) in sections) {
					val firstPoint = Projections.Two.projectAngleOntoUnitBox(sectionStart.toDouble())
					val secondPoint = Projections.Two.projectAngleOntoUnitBox(sectionEnd.toDouble())
					fun ilerp(f: Float): Float =
						ilerp(-1f, 1f, f)

					bufferBuilder
						.vertex(matrix, secondPoint.x, secondPoint.y, 0F)
						.texture(lerp(u1, u2, ilerp(secondPoint.x)), lerp(v1, v2, ilerp(secondPoint.y)))
						.color(color)

					bufferBuilder
						.vertex(matrix, firstPoint.x, firstPoint.y, 0F)
						.texture(lerp(u1, u2, ilerp(firstPoint.x)), lerp(v1, v2, ilerp(firstPoint.y)))
						.color(color)

					bufferBuilder
						.vertex(matrix, 0F, 0F, 0F)
						.texture(lerp(u1, u2, ilerp(0F)), lerp(v1, v2, ilerp(0F)))
						.color(color)

				}

				bufferBuilder.end().use { buffer ->
					if (state.innerCutoutRadius <= 0) {
						state.layer.draw(buffer)
						return
					}
					CustomRenderPassHelper(
						{ "RenderCircleProgress" },
						VertexFormat.DrawMode.TRIANGLES,
						state.layer.vertexFormat,
						MC.instance.framebuffer,
						false,
					).use { renderPass ->
						renderPass.uploadVertices(buffer)
						renderPass.setAllDefaultUniforms()
						renderPass.setPipeline(state.layer.pipeline)
						renderPass.setUniform("CutoutRadius", 4) {
							it.putFloat(state.innerCutoutRadius)
						}
						renderPass.draw()
					}
				}
			}
			matrices.pop()
		}

		override fun getElementClass(): Class<State> {
			return State::class.java
		}

		override fun getName(): String {
			return "Firmament Circle"
		}
	}

	fun renderCircularSlice(
		drawContext: DrawContext,
		layer: RenderLayer.MultiPhase,
		u1: Float,
		u2: Float,
		v1: Float,
		v2: Float,
		angleRadians: ClosedFloatingPointRange<Float>,
		color: Int = -1,
		innerCutoutRadius: Float = 0F
	) {
		val screenRect = ScreenRect(-1, -1, 2, 2).transform(drawContext.matrices)
		drawContext.state.addSpecialElement(
			State(
				screenRect.left, screenRect.right,
				screenRect.top, screenRect.bottom,
				layer,
				u1, u2, v1, v2,
				angleRadians,
				color,
				innerCutoutRadius,
				screenRect.width / 2F,
				screenRect,
				null
			)
		)
	}

	fun renderCircle(
		drawContext: DrawContext,
		texture: Identifier,
		progress: Float,
		u1: Float,
		u2: Float,
		v1: Float,
		v2: Float,
		color: Int = -1
	) {
		renderCircularSlice(
			drawContext,
			CustomRenderLayers.GUI_TEXTURED_NO_DEPTH_TRIS.apply(texture),
			u1, u2, v1, v2,
			(-τ / 4).toFloat()..(progress * τ - τ / 4).toFloat(),
			color = color
		)
	}
}
