package moe.nea.firmament.mixins;

import moe.nea.firmament.events.HandledScreenKeyPressedEvent;
import moe.nea.firmament.events.IsSlotProtectedEvent;
import moe.nea.firmament.events.SlotRenderEvents;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(HandledScreen.class)
public class MixinHandledScreen {

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;handleHotbarKeyPressed(II)Z", shift = At.Shift.BEFORE), cancellable = true)
    public void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (HandledScreenKeyPressedEvent.Companion.publish(new HandledScreenKeyPressedEvent(keyCode, scanCode, modifiers)).getCancelled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    public void onMouseClickedSlot(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (IsSlotProtectedEvent.shouldBlockInteraction(slot)) {
            ci.cancel();
        }
    }


    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlot(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/screen/slot/Slot;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onAfterDrawSlot(
        MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci,
        int i, int j, int k, Slot slot) {
        SlotRenderEvents.After event = new SlotRenderEvents.After(matrices, slot, mouseX, mouseY, delta);
        SlotRenderEvents.After.Companion.publish(event);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlot(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/screen/slot/Slot;)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onBeforeDrawSlot(
        MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci,
        int i, int j, int k, Slot slot) {
        SlotRenderEvents.Before event = new SlotRenderEvents.Before(matrices, slot, mouseX, mouseY, delta);
        SlotRenderEvents.Before.Companion.publish(event);
    }
}
