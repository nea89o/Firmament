package moe.nea.firmament.util.render;

import java.util.Optional;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import moe.nea.firmament.Firmament;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;

public class CustomRenderPipelines {
	public static final RenderPipeline GUI_TEXTURED_NO_DEPTH_TRIANGLES = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(Firmament.identifier("gui_textured_overlay_tris"))
		.withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
		.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
		.withDepthStencilState(Optional.empty())
		.withCull(false)
		.build();
	public static final RenderPipeline GUI_TEXTURED_NO_DEPTH_TRIANGLES_CIRCLE = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(Firmament.identifier("gui_textured_overlay_tris"))
		.withVertexBinding(0, CustomVertexFormats.POSITION_TEX_COLOR_RADIUS)
		.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
		.withDepthStencilState(Optional.empty())
		.withCull(false)
		.build();
	public static final RenderPipeline COLORED_OMNIPRESENT_QUADS = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
		.withLocation(Firmament.identifier("colored_omnipresent_quads"))
		.withVertexShader("core/position_color")
		.withFragmentShader("core/position_color")
		.withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
		.withPrimitiveTopology(PrimitiveTopology.QUADS)
		.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
		.withDepthStencilState(Optional.empty())
		.withCull(false)
		.build();
	public static final RenderPipeline CIRCLE_FILTER_TRANSLUCENT_GUI_TRIANGLES = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
		.withLocation(Firmament.identifier("gui_textured_overlay_tris_circle"))
		.withVertexShader(Firmament.identifier("circle_discard_color"))
		.withFragmentShader(Firmament.identifier("circle_discard_color"))
		.withVertexBinding(0, CustomVertexFormats.POSITION_TEX_COLOR_RADIUS)
		.withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
		.build();
	public static final RenderPipeline PARALLAX_CAPE = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
		.withLocation(Firmament.identifier("parallax_cape"))
		.withFragmentShader(Firmament.identifier("cape/parallax"))
		.withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER1_SAMPLER2)
		.withBindGroupLayout(CustomBindGroupLayouts.ANIMATION_DATA)
		.build();
	public static final RenderPipeline OMNIPRESENT_LINES = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
		.withLocation(Firmament.identifier("omnipresent_lines"))
		.withDepthStencilState(Optional.empty())
		.build();
}
