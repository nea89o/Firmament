package moe.nea.firmament.mixins.render.renderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import moe.nea.firmament.util.render.FirmamentRenderSystem;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public class CloseRendererMixin {
	@Inject(method = "close", at = @At("TAIL"))
	private void onCloseRenderer(CallbackInfo ci) {
		FirmamentRenderSystem.close();
	}
}
