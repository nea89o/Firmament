package moe.nea.firmament.util.render;

import java.util.function.Function;

import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class CustomRenderTypes {
	public static final Function<Identifier, RenderType> GUI_TEXTURED_NO_DEPTH_TRIANGLES = Util.memoize(texture -> RenderType.create("firmament_gui_textured_overlay_tris", RenderSetup.builder(CustomRenderPipelines.GUI_TEXTURED_NO_DEPTH_TRIANGLES)
			.withTexture("Sampler0", texture)
			.createRenderSetup()
	));
	public static final RenderType COLORED_QUADS = RenderType.create("firmament_quads", RenderSetup.builder(CustomRenderPipelines.COLORED_OMNIPRESENT_QUADS).createRenderSetup());
	public static final RenderType LINES_NO_DEPTH = RenderType.create("firmament_lines_no_depth", RenderSetup.builder(CustomRenderPipelines.OMNIPRESENT_LINES)
			.setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			.createRenderSetup()
	);
	public static final Function<Identifier, RenderType> GUI_TEXTURED_NO_DEPTH_TRIANGLES_CIRCLE = Util.memoize(texture -> RenderType.create("firmament_gui_textured_overlay_tris_circle", RenderSetup.builder(CustomRenderPipelines.GUI_TEXTURED_NO_DEPTH_TRIANGLES_CIRCLE)
		.withTexture("Sampler0", texture)
		.createRenderSetup()
	));
	public static final RenderType TRANSLUCENT_CIRCLE_GUI = RenderType.create("firmament_translucent_circle_gui", RenderSetup.builder(CustomRenderPipelines.CIRCLE_FILTER_TRANSLUCENT_GUI_TRIANGLES).createRenderSetup());
}
