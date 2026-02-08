

package moe.nea.firmament.mixins;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import moe.nea.firmament.events.WorldRenderLastEvent;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.LevelRenderState;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
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
	protected abstract void checkPoseStack(PoseStack matrices);

	@Shadow
	private int ticks;

	@Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", shift = At.Shift.AFTER))
	public void onWorldRenderLast(GpuBufferSlice gpuBufferSlice, LevelRenderState levelRenderState, ProfilerFiller profilerFiller, Matrix4f matrix4f, ResourceHandle resourceHandle, ResourceHandle resourceHandle2, boolean bl, ResourceHandle resourceHandle3, ResourceHandle resourceHandle4, CallbackInfo ci) {
		var imm = this.renderBuffers.bufferSource();
		var stack = new PoseStack();
		// TODO: pre-cancel this event if F1 is active
		var event = new WorldRenderLastEvent(
			stack, ticks,
			levelRenderState.cameraRenderState,
			imm
		);
		WorldRenderLastEvent.Companion.publish(event);
		imm.endBatch();
		checkPoseStack(stack);
	}
}
