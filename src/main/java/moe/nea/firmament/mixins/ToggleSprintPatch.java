

package moe.nea.firmament.mixins;

import moe.nea.firmament.features.fixes.Fixes;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class ToggleSprintPatch {
    @Inject(method = "isPressed", at = @At("HEAD"), cancellable = true)
    public void onIsPressed(CallbackInfoReturnable<Boolean> cir) {
        Fixes.INSTANCE.handleIsPressed((KeyBinding) (Object) this, cir);
    }
}
