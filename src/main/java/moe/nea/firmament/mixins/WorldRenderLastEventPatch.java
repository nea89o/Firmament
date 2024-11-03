

package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.events.WorldRenderLastEvent;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Handle;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.profiler.Profiler;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRenderLastEventPatch {
	@Shadow
	@Final
	private BufferBuilderStorage bufferBuilders;

	@Shadow
	@Final
	private DefaultFramebufferSet framebufferSet;

	@Shadow
	protected abstract void checkEmpty(MatrixStack matrices);

	@Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V", shift = At.Shift.AFTER))
	public void onWorldRenderLast(Fog fog, RenderTickCounter tickCounter, Camera camera, Profiler profiler, Matrix4f matrix4f, Matrix4f matrix4f2, Handle handle, Handle handle2, Handle handle3, Handle handle4, boolean bl, Frustum frustum, Handle handle5, CallbackInfo ci) {
		var imm = this.bufferBuilders.getEntityVertexConsumers();
		var stack = new MatrixStack();
		// TODO: pre-cancel this event if F1 is active
		var event = new WorldRenderLastEvent(
			stack, tickCounter,
			camera,
			imm
		);
		WorldRenderLastEvent.Companion.publish(event);
		imm.draw();
		checkEmpty(stack);
	}
}
