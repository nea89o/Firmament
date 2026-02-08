package moe.nea.firmament.util.render

import com.mojang.blaze3d.vertex.VertexFormat
import util.render.CustomRenderLayers
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.navigation.ScreenRectangle
import com.mojang.blaze3d.vertex.BufferBuilder
import net.minecraft.client.renderer.MultiBufferSource
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.resources.Identifier
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
		val layer: RenderType,
		val u1: Float,
		val u2: Float,
		val v1: Float,
		val v2: Float,
		val angleRadians: ClosedFloatingPointRange<Float>,
		val color: Int,
		val innerCutoutRadius: Float,
		override val scale: Float,
		override val bounds: ScreenRectangle?,
		override val scissorArea: ScreenRectangle?,
	) : MultiSpecialGuiRenderState() {
		override fun createRenderer(vertexConsumers: MultiBufferSource.BufferSource): MultiSpecialGuiRenderer<out MultiSpecialGuiRenderState> {
			return Renderer(vertexConsumers)
		}
	}

	class Renderer(vertexConsumers: MultiBufferSource.BufferSource) :
		MultiSpecialGuiRenderer<State>(vertexConsumers) {
		override fun renderToTexture(
			state: State,
			matrices: PoseStack
		) {
			matrices.pushPose()
			matrices.translate(0F, -1F, 0F)
			val sections = state.angleRadians.nonNegligibleSubSectionsAlignedWith((τ / 8f).toFloat())
				.zipWithNext().toList()
			if (sections.isEmpty()) return
			val u1 = state.u1
			val u2 = state.u2
			val v1 = state.v1
			val v2 = state.v2
			val color = state.color
			val matrix = matrices.last().pose()
			ByteBufferBuilder(state.layer.format().vertexSize * sections.size * 3).use { allocator ->

				val bufferBuilder = BufferBuilder(allocator, VertexFormat.Mode.TRIANGLES, state.layer.format())

				for ((sectionStart, sectionEnd) in sections) {
					val firstPoint = Projections.Two.projectAngleOntoUnitBox(sectionStart.toDouble())
					val secondPoint = Projections.Two.projectAngleOntoUnitBox(sectionEnd.toDouble())
					fun ilerp(f: Float): Float =
						ilerp(-1f, 1f, f)

					bufferBuilder
						.addVertex(matrix, secondPoint.x, secondPoint.y, 0F)
						.setUv(lerp(u1, u2, ilerp(secondPoint.x)), lerp(v1, v2, ilerp(secondPoint.y)))
						.setColor(color)

					bufferBuilder
						.addVertex(matrix, firstPoint.x, firstPoint.y, 0F)
						.setUv(lerp(u1, u2, ilerp(firstPoint.x)), lerp(v1, v2, ilerp(firstPoint.y)))
						.setColor(color)

					bufferBuilder
						.addVertex(matrix, 0F, 0F, 0F)
						.setUv(lerp(u1, u2, ilerp(0F)), lerp(v1, v2, ilerp(0F)))
						.setColor(color)

				}

				bufferBuilder.buildOrThrow().use { buffer ->
					if (state.innerCutoutRadius <= 0) {
						state.layer.draw(buffer)
						return
					}
					CustomRenderPassHelper(
						{ "RenderCircleProgress" },
						VertexFormat.Mode.TRIANGLES,
						state.layer.format(),
						MC.instance.mainRenderTarget,
						false,
					).use { renderPass ->
						renderPass.uploadVertices(buffer)
						renderPass.setAllDefaultUniforms()
						renderPass.setPipeline(state.layer.pipeline())
						renderPass.setUniform("CutoutRadius", 4) {
							it.putFloat(state.innerCutoutRadius)
						}
						renderPass.draw()
					}
				}
			}
			matrices.popPose()
		}

		override fun getRenderStateClass(): Class<State> {
			return State::class.java
		}

		override fun getTextureLabel(): String {
			return "Firmament Circle"
		}
	}

	fun renderCircularSlice(
		drawContext: GuiGraphics,
		layer: RenderType,
		u1: Float,
		u2: Float,
		v1: Float,
		v2: Float,
		angleRadians: ClosedFloatingPointRange<Float>,
		color: Int = -1,
		innerCutoutRadius: Float = 0F
	) {
		val screenRect = ScreenRectangle(-1, -1, 2, 2).transformAxisAligned(drawContext.pose())
		drawContext.guiRenderState.submitPicturesInPictureState(
			State(
				screenRect.left(), screenRect.right(),
				screenRect.top(), screenRect.bottom(),
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
		drawContext: GuiGraphics,
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
