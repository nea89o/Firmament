package util.render

import com.mojang.blaze3d.PrimitiveTopology
import com.mojang.blaze3d.pipeline.BindGroupLayout
import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.ColorTargetState
import com.mojang.blaze3d.pipeline.DepthStencilState
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.CompareOp

import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import java.util.function.Function
import net.minecraft.client.renderer.BindGroupLayouts
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.rendertype.LayeringTransform
import net.minecraft.client.renderer.rendertype.OutputTarget
import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.resources.Identifier
import net.minecraft.util.Util
import moe.nea.firmament.Firmament

object CustomRenderPipelines {
	private val NO_DEPTH_TEST = DepthStencilState(CompareOp.ALWAYS_PASS, false)

	val GUI_TEXTURED_NO_DEPTH_TRIS =
		RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
			.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
			.withLocation(Firmament.identifier("gui_textured_overlay_tris"))
			.withDepthStencilState(NO_DEPTH_TEST)
			.withCull(false)
			.build()
	val COLORED_OMNIPRESENT_QUADS =
		RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)// TODO: split this up to support better transparent ordering.
			.withLocation(Firmament.identifier("colored_omnipresent_quads"))
			.withVertexShader("core/position_color")
			.withFragmentShader("core/position_color")
			.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.QUADS)
			.withDepthStencilState(NO_DEPTH_TEST)
			.withCull(false)
			.withColorTargetState(ColorTargetState(BlendFunction.TRANSLUCENT))
			.build()

	val CUTOUT_DATA = BindGroupLayout.builder()
		.withUniform("CutoutRadius", UniformType.UNIFORM_BUFFER)
		.build()

	val CIRCLE_FILTER_TRANSLUCENT_GUI_TRIS =
		RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
			.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
			.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
			.withLocation(Firmament.identifier("gui_textured_overlay_tris_circle"))
			.withBindGroupLayout(CUTOUT_DATA)
			.withFragmentShader(Firmament.identifier("circle_discard_color"))
//			.withBlend(BlendFunction.TRANSLUCENT)
			.build()

	val ANIMATION_DATA = BindGroupLayout.builder()
		.withUniform("Animation", UniformType.UNIFORM_BUFFER)
		.build()

	val PARALLAX_CAPE_SHADER =
		RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
			.withLocation(Firmament.identifier("parallax_cape"))
			.withFragmentShader(Firmament.identifier("cape/parallax"))
			.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
			.withBindGroupLayout(BindGroupLayouts.SAMPLER1)
			.withBindGroupLayout(BindGroupLayouts.SAMPLER2)
			.withBindGroupLayout(ANIMATION_DATA)
			.build()
	val OMNIPRESENT_LINES = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
		.withDepthStencilState(NO_DEPTH_TEST)
		.withLocation(Firmament.identifier("lines"))
		.withLocation("pipeline/lines").build()
}

object CustomRenderLayers {
	inline fun memoizeTextured(crossinline func: (Identifier) -> RenderType) = memoize(func)
	inline fun <T : Any, R : Any> memoize(crossinline func: (T) -> R): Function<T, R> {
		return Util.memoize { it: T -> func(it) }
	}

	val GUI_TEXTURED_NO_DEPTH_TRIS = memoizeTextured { texture ->
		RenderType.create(
			"firmament_gui_textured_overlay_tris",
			RenderSetup.builder(CustomRenderPipelines.GUI_TEXTURED_NO_DEPTH_TRIS)
				.bufferSize(RenderType.TRANSIENT_BUFFER_SIZE)
				.withTexture("Sampler0", texture)
				.createRenderSetup()
		)
	}

	//	val LINES = RenderType.create(
//		"firmament_lines",
//		RenderType.TRANSIENT_BUFFER_SIZE,
//		CustomRenderPipelines.OMNIPRESENT_LINES,
//		RenderType.CompositeState.builder() // TODO: accept linewidth here
//			.createCompositeState(false)
//	)
	val COLORED_QUADS = RenderType.create(
		"firmament_quads",
		RenderSetup
			.builder(CustomRenderPipelines.COLORED_OMNIPRESENT_QUADS)
			.bufferSize(RenderType.TRANSIENT_BUFFER_SIZE)
			.createRenderSetup()
	)

	val LINES_NO_DEPTH = RenderType.create(
		"firmament_lines_no_depth",
		RenderSetup.builder(CustomRenderPipelines.OMNIPRESENT_LINES)
			.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			.createRenderSetup()
	)

	val TRANSLUCENT_CIRCLE_GUI =
		RenderType.create(
			"firmament_translucent_circle_gui",
			RenderSetup.builder(CustomRenderPipelines.CIRCLE_FILTER_TRANSLUCENT_GUI_TRIS)
				.bufferSize(RenderType.TRANSIENT_BUFFER_SIZE)
				.createRenderSetup()
		)
}
