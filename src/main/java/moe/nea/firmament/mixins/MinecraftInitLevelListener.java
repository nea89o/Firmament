package moe.nea.firmament.mixins;

import moe.nea.firmament.util.mc.InitLevel;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftInitLevelListener {
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;initBackendSystem()Lnet/minecraft/util/TimeSupplier$Nanoseconds;"))
	private void onInitRenderBackend(CallbackInfo ci) {
		InitLevel.bump(InitLevel.RENDER_INIT);
	}

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;initRenderer(JIZLjava/util/function/BiFunction;Z)V"))
	private void onInitRender(CallbackInfo ci) {
		InitLevel.bump(InitLevel.RENDER);
	}

	@Inject(method = "method_29339", at = @At(value = "HEAD"))
	private void onFinishedLoading(CallbackInfo ci) {
		InitLevel.bump(InitLevel.MAIN_MENU);
	}
}
