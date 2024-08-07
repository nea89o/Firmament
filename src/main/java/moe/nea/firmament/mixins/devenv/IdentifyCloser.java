
package moe.nea.firmament.mixins.devenv;

import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class IdentifyCloser {
    @Inject(method = "close", at = @At("HEAD"))
    public void onClose(CallbackInfo ci) {
        Thread.dumpStack();
    }
}
