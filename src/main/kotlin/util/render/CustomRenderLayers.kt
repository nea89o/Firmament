package util.render

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode
import java.util.function.Function
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.Identifier
import net.minecraft.util.TriState
import net.minecraft.util.Util
import moe.nea.firmament.Firmament

object CustomRenderPipelines {
	val GUI_TEXTURED_NO_DEPTH_TRIS =
		RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
			.withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, DrawMode.TRIANGLES)
			.withLocation(Firmament.identifier("gui_textured_overlay_tris"))
			.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
			.withCull(false)
			.withDepthWrite(false)
			.build()
	val COLORED_OMNIPRESENT_QUADS = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)// TODO: split this up to support better transparent ordering.
		.withLocation(Firmament.identifier("colored_omnipresent_quads"))
		.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
		.withDepthWrite(false)
		.build()
}

object CustomRenderLayers {


	inline fun memoizeTextured(crossinline func: (Identifier) -> RenderLayer) = memoize(func)
	inline fun <T, R> memoize(crossinline func: (T) -> R): Function<T, R> {
		return Util.memoize { it: T -> func(it) }
	}

	val GUI_TEXTURED_NO_DEPTH_TRIS = memoizeTextured { texture ->
		RenderLayer.of("firmament_gui_textured_overlay_tris",
		               RenderLayer.DEFAULT_BUFFER_SIZE,
		               CustomRenderPipelines.GUI_TEXTURED_NO_DEPTH_TRIS,
		               RenderLayer.MultiPhaseParameters.builder().texture(
			               RenderPhase.Texture(texture, TriState.DEFAULT, false))
			               .build(false))
	}
	val LINES = RenderLayer.of(
		"firmament_lines",
		RenderLayer.DEFAULT_BUFFER_SIZE,
		RenderPipelines.LINES,
		RenderLayer.MultiPhaseParameters.builder()
			.build(false)
	)
	val COLORED_QUADS = RenderLayer.of(
		"firmament_quads",
		RenderLayer.DEFAULT_BUFFER_SIZE,
		CustomRenderPipelines.COLORED_OMNIPRESENT_QUADS,
		RenderLayer.MultiPhaseParameters.builder()
			.build(false)
	)
}
