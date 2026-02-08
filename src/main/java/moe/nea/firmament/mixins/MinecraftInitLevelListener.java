package moe.nea.firmament.mixins;

import moe.nea.firmament.util.mc.InitLevel;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftInitLevelListener {
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;initBackendSystem()Lnet/minecraft/util/TimeSource$NanoTimeSource;"))
	private void onInitRenderBackend(CallbackInfo ci) {
		InitLevel.bump(InitLevel.RENDER_INIT);
	}

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;initRenderer(JIZLcom/mojang/blaze3d/shaders/ShaderSource;Z)V"))
	private void onInitRender(CallbackInfo ci) {
		InitLevel.bump(InitLevel.RENDER);
	}

	@Inject(method = "<init>", at = @At(value = "TAIL"))
	private void onFinishedLoading(CallbackInfo ci) {
		InitLevel.bump(InitLevel.MAIN_MENU);
	}
}
