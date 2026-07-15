package moe.nea.firmament.util.render.gui.pip;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;

import moe.nea.firmament.mixins.accessor.AccessorGameRenderer;
import moe.nea.firmament.util.render.state.gui.RenderLineState;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.GlobalSettingsUniform;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;

public class RenderLineRenderer extends PictureInPictureRenderer<RenderLineState> {
	private RenderLineRenderer(PictureInPictureRendererRegistry.Context context) {}

	public static void init() {
		PictureInPictureRendererRegistry.register(RenderLineRenderer::new);
	}

	@Override
	public Class<RenderLineState> getRenderStateClass() {
		return RenderLineState.class;
	}

	public void prepare(RenderLineState state, GuiRenderState guiRenderState, FeatureRenderDispatcher featureRenderDispatcher, int guiScale) {
		Minecraft minecraft = Minecraft.getInstance();
		GameRenderer gameRenderer = minecraft.gameRenderer;
		GameRenderState gameRenderState = gameRenderer.gameRenderState();

		// TODO: is this viewport mangling still needed with the new line shader in 1.21.11
		GlobalSettingsUniform globalSettingsUniform = ((AccessorGameRenderer) gameRenderer).getGlobalSettingsUniform_Firmament();
		globalSettingsUniform.update(
			state.bounds().width(),
			state.bounds().height(),
			gameRenderState.optionsRenderState.glintStrength,
			minecraft.level == null ? 0L : minecraft.level.getGameTime(),
			minecraft.getDeltaTracker(),
			gameRenderState.optionsRenderState.menuBackgroundBlurriness,
			gameRenderState.levelRenderState.cameraRenderState.pos,
			gameRenderState.optionsRenderState.textureFiltering == TextureFilteringMethod.RGSS
		);

		super.prepare(state, guiRenderState, featureRenderDispatcher, guiScale);

		globalSettingsUniform.update(
			gameRenderState.windowRenderState.width,
			gameRenderState.windowRenderState.height,
			gameRenderState.optionsRenderState.glintStrength,
			minecraft.level == null ? 0L : minecraft.level.getGameTime(),
			minecraft.getDeltaTracker(),
			gameRenderState.optionsRenderState.menuBackgroundBlurriness,
			gameRenderState.levelRenderState.cameraRenderState.pos,
			gameRenderState.optionsRenderState.textureFiltering == TextureFilteringMethod.RGSS
		);
	}

	@Override
	protected void renderToTexture(RenderLineState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
		submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.LINES, (pose, buffer) -> {
			float wh = state.width() / 2f;
			float hh = state.height() / 2f;
			float lowX = -wh;
			float lowY = state.direction() == RenderLineState.LineDirection.BOTTOM_LEFT_TO_TOP_RIGHT ? hh : -hh;
			float highX = wh;
			float highY = -lowY;
			Vector3f norm = new Vector3f(highX - lowX, highY - lowY, 0F).normalize();

			buffer
				.addVertex(pose, lowX, lowY, 0F)
				.setNormal(pose, norm)
				.setLineWidth(state.lineWidth());

			buffer
				.addVertex(pose, highX, highY, 0F)
				.setNormal(pose, norm)
				.setLineWidth(state.lineWidth());
		});
	}

	@Override
	protected float getTranslateY(int height, int guiScale) {
		return height / 2f;
	}

	@Override
	protected String getTextureLabel() {
		return "Firmament Line Renderer";
	}
}
