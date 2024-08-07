

package moe.nea.firmament.mixins;

import kotlin.Pair;
import moe.nea.firmament.features.inventory.SaveCursorPosition;
import net.minecraft.client.Mouse;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class SaveCursorPositionPatch {
    @Shadow
    private double x;

    @Shadow
    private double y;

    @Inject(method = "lockCursor", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/Mouse;cursorLocked:Z"))
    public void onLockCursor(CallbackInfo ci) {
        SaveCursorPosition.saveCursorOriginal(x, y);
    }

    @Inject(method = "lockCursor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getHandle()J"))
    public void onLockCursorAfter(CallbackInfo ci) {
        SaveCursorPosition.saveCursorMiddle(x, y);
    }

    @Inject(method = "unlockCursor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;getHandle()J"))
    public void onUnlockCursor(CallbackInfo ci) {
        Pair<Double, Double> cursorPosition = SaveCursorPosition.loadCursor(this.x, this.y);
        if (cursorPosition == null) return;
        this.x = cursorPosition.getFirst();
        this.y = cursorPosition.getSecond();
    }
}
