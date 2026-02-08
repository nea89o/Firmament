package moe.nea.firmament.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.Camera;

// This mixin injects into GameRenderer.getFov(Camera, float, boolean) and applies our multiplier
@Mixin(GameRenderer.class)
public abstract class GameRendererFovPatch {
    @Inject(
        method = "getFov(Lnet/minecraft/client/Camera;FZ)F",
        at = @At("RETURN"),
        cancellable = true
    )
    private void onGetFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        try {
            float base = cir.getReturnValueF();
            float mult = moe.nea.firmament.features.items.EtherwarpOverlay.getFovMultiplier(tickDelta);
            cir.setReturnValue(base * mult);
        } catch (Throwable t) {
            // on any error, keep base value
        }
    }
}
