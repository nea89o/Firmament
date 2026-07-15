package moe.nea.firmament.util.render.state.gui;

import kotlin.ranges.ClosedFloatingPointRange;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

public record RenderCircleProgressState(
	RenderType type,
	int x0,
	int y0,
	int x1,
	int y1,
	float u1,
	float v1,
	float u2,
	float v2,
	ClosedFloatingPointRange<Float> angleRadians,
	int colour,
	float innerCutoutRadius,
	float scale,
	ScreenRectangle bounds,
	@Nullable ScreenRectangle scissorArea
) implements PictureInPictureRenderState {
}
