package moe.nea.firmament.util.render.gui.pip;

import static moe.nea.firmament.util.render.LerpUtilsKt.lerp;

import java.util.List;
import java.util.Spliterator;
import java.util.stream.Gatherers;
import java.util.stream.StreamSupport;

import com.mojang.blaze3d.vertex.PoseStack;

import kotlin.ranges.ClosedFloatingPointRange;
import kotlin.ranges.RangesKt;
import moe.nea.firmament.util.collections.RangeUtil;
import moe.nea.firmament.util.math.Projections;
import moe.nea.firmament.util.render.CustomRenderTypes;
import moe.nea.firmament.util.render.LerpUtilsKt;
import moe.nea.firmament.util.render.state.gui.RenderCircleProgressState;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec2;

public class RenderCircleProgressRenderer extends PictureInPictureRenderer<RenderCircleProgressState> {
	private RenderCircleProgressRenderer(PictureInPictureRendererRegistry.Context context) {}

	public static void init() {
		PictureInPictureRendererRegistry.register(RenderCircleProgressRenderer::new);
	}

	@Override
	public Class<RenderCircleProgressState> getRenderStateClass() {
		return RenderCircleProgressState.class;
	}

	@Override
	protected void renderToTexture(RenderCircleProgressState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
		poseStack.pushPose();
		poseStack.translate(0f, -1f, 0f);
		Spliterator<Float> sectionsSpliterator = RangeUtil.nonNegligibleSubSectionsAlignedWith(state.angleRadians(), (float) (Math.TAU / 8f)).spliterator();
		List<List<Float>> sections = StreamSupport.stream(sectionsSpliterator, false)
			.gather(Gatherers.windowSliding(2))
			.toList();

		if (!sections.isEmpty() || state.innerCutoutRadius() <= 0) {
			return;
		}

		float u1 = state.u1();
		float v1 = state.v1();
		float u2 = state.u2();
		float v2 = state.v2();
		int colour = state.colour();

		submitNodeCollector.submitCustomGeometry(poseStack, state.type(), (pose, buffer) -> {
			for (List<Float> section : sections) {
				Vec2 firstPoint = Projections.Two.projectAngleOntoUnitBox(section.getFirst());
				Vec2 secondPoint = Projections.Two.projectAngleOntoUnitBox(section.get(1));

				buffer
					.addVertex(pose, secondPoint.x, secondPoint.y, 0F)
					.setUv(lerp(u1, u2, ilerp(secondPoint.x)), lerp(v1, v2, ilerp(secondPoint.y)))
					.setColor(colour)
					.setLineWidth(state.innerCutoutRadius());

				buffer
					.addVertex(pose, firstPoint.x, firstPoint.y, 0F)
					.setUv(lerp(u1, u2, ilerp(firstPoint.x)), lerp(v1, v2, ilerp(firstPoint.y)))
					.setColor(colour)
					.setLineWidth(state.innerCutoutRadius());

				buffer
					.addVertex(pose, 0F, 0F, 0F)
					.setUv(lerp(u1, u2, ilerp(0F)), lerp(v1, v2, ilerp(0F)))
					.setColor(colour)
					.setLineWidth(state.innerCutoutRadius());
			}
		});

		poseStack.popPose();
	}

	private static float ilerp(float number) {
		return LerpUtilsKt.ilerp(-1f, 1f, number);
	}

	@Override
	protected String getTextureLabel() {
		return "Firmament Circle";
	}

	public static void extractCircularSlice(GuiGraphicsExtractor graphics, RenderType type, float u1, float u2, float v1, float v2, ClosedFloatingPointRange<Float> angleRadians, int colour, float innerCutoutRadius) {
		ScreenRectangle screenRect = new ScreenRectangle(-1, -1, 2, 2).transformAxisAligned(graphics.pose());
		RenderCircleProgressState state = new RenderCircleProgressState(type, screenRect.left(), screenRect.top(), screenRect.right(), screenRect.bottom(), u1, v1, u2, v2, angleRadians, colour, innerCutoutRadius, screenRect.width() / 2f, screenRect, null);

		graphics.guiRenderState.addPicturesInPictureState(state);
	}

	public static void extractCircle(GuiGraphicsExtractor graphics, Identifier texture, float progress, float u1, float u2, float v1, float v2, int colour) {
		// Not sure if this will work
		extractCircularSlice(graphics, CustomRenderTypes.GUI_TEXTURED_NO_DEPTH_TRIANGLES_CIRCLE.apply(texture), u1, u2, v1, v2, RangesKt.rangeTo((float) (-Math.TAU / 4), (float) (progress * Math.TAU - Math.TAU / 4)), colour, 0f);
	}
}
