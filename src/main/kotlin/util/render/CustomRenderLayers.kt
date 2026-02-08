package util.render

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.vertex.VertexFormat.Mode
import java.util.function.Function
import net.minecraft.client.renderer.RenderPipelines
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.resources.Identifier
import net.minecraft.util.Util
import moe.nea.firmament.Firmament

object CustomRenderPipelines {
	val GUI_TEXTURED_NO_DEPTH_TRIS =
		RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
			.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, Mode.TRIANGLES)
			.withLocation(Firmament.identifier("gui_textured_overlay_tris"))
			.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
			.withCull(false)
			.withDepthWrite(false)
			.build()
	val OMNIPRESENT_LINES = RenderPipeline
		.builder(RenderPipelines.LINES_SNIPPET)
		.withLocation(Firmament.identifier("lines"))
		.withDepthWrite(false)
		.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
		.build()
	val COLORED_OMNIPRESENT_QUADS =
		RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)// TODO: split this up to support better transparent ordering.
			.withLocation(Firmament.identifier("colored_omnipresent_quads"))
			.withVertexShader("core/position_color")
			.withFragmentShader("core/position_color")
			.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, Mode.QUADS)
			.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
			.withCull(false)
			.withDepthWrite(false)
			.withBlend(BlendFunction.TRANSLUCENT)
			.build()

	val CIRCLE_FILTER_TRANSLUCENT_GUI_TRIS =
		RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
			.withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, Mode.TRIANGLES)
			.withLocation(Firmament.identifier("gui_textured_overlay_tris_circle"))
			.withUniform("CutoutRadius", UniformType.UNIFORM_BUFFER)
			.withFragmentShader(Firmament.identifier("circle_discard_color"))
//			.withBlend(BlendFunction.TRANSLUCENT)
			.build()
	val PARALLAX_CAPE_SHADER =
		RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
			.withLocation(Firmament.identifier("parallax_cape"))
			.withFragmentShader(Firmament.identifier("cape/parallax"))
			.withSampler("Sampler0")
			.withSampler("Sampler1")
			.withSampler("Sampler3")
			.withUniform("Animation", UniformType.UNIFORM_BUFFER)
			.build()
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

	val TRANSLUCENT_CIRCLE_GUI =
		RenderType.create(
			"firmament_translucent_circle_gui",
			RenderSetup.builder(CustomRenderPipelines.CIRCLE_FILTER_TRANSLUCENT_GUI_TRIS)
				.bufferSize(RenderType.TRANSIENT_BUFFER_SIZE)
				.createRenderSetup()
		)
}
