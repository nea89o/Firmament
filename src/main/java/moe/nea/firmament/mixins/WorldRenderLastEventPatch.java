

package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import moe.nea.firmament.events.WorldRenderLastEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class WorldRenderLastEventPatch {
	@Shadow
	@Final
	private RenderBuffers renderBuffers;

	@Shadow
	protected abstract void checkPoseStack(PoseStack poseStack);

	@Shadow
	private int ticks;

	@Shadow
	@Final
	private LevelTargetBundle targets;

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", shift = At.Shift.AFTER))
	public void onWorldRenderLast(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, ChunkSectionsToRender chunkSectionsToRender, CallbackInfo ci, @Local(name = "frame") FrameGraphBuilder frame) {

		var pass = frame.addPass("FirmamentWorldRenderLast");
		this.targets.main = pass.readsAndWrites(this.targets.main);
		if (this.targets.itemEntity != null) {
			this.targets.itemEntity = pass.readsAndWrites(this.targets.itemEntity);
		}
		var mainTarget = this.targets.main;

		pass.executes(() -> {
			var stack = new PoseStack();
			var imm = this.renderBuffers.bufferSource();
			RenderSystem.outputColorTextureOverride = mainTarget.get().getColorTextureView();
			RenderSystem.outputDepthTextureOverride = mainTarget.get().getDepthTextureView();
			// TODO: pre-cancel this event if F1 is active
			var event = new WorldRenderLastEvent(
				stack, ticks,
				cameraState,
				imm
			);
			WorldRenderLastEvent.Companion.publish(event);
			imm.endLastBatch();

			RenderSystem.outputColorTextureOverride = null;
			RenderSystem.outputDepthTextureOverride = null;

			checkPoseStack(stack);
		});
	}
}
