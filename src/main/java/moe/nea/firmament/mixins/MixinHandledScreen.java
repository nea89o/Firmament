/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins;

import moe.nea.firmament.events.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen<T extends ScreenHandler> {

    @Shadow
    @Final
    protected T handler;

    @Shadow
    public abstract T getScreenHandler();

    @Shadow
    protected int y;
    @Shadow
    protected int x;
    @Unique
    PlayerInventory playerInventory;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void savePlayerInventory(ScreenHandler handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
        this.playerInventory = inventory;
    }

    @Inject(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;handleHotbarKeyPressed(II)Z", shift = At.Shift.BEFORE), cancellable = true)
    public void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (HandledScreenKeyPressedEvent.Companion.publish(new HandledScreenKeyPressedEvent((HandledScreen<?>) (Object) this, keyCode, scanCode, modifiers)).getCancelled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (HandledScreenClickEvent.Companion.publish(new HandledScreenClickEvent((HandledScreen<?>) (Object) this, mouseX, mouseY, button)).getCancelled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawForeground(Lnet/minecraft/client/gui/DrawContext;II)V", shift = At.Shift.AFTER))
    public void onAfterRenderForeground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.getMatrices().push();
        context.getMatrices().translate(-x, -y, 0);
        HandledScreenForegroundEvent.Companion.publish(new HandledScreenForegroundEvent((HandledScreen<?>) (Object) this, context, mouseX, mouseY, delta));
        context.getMatrices().pop();
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    public void onMouseClickedSlot(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (slotId == -999 && getScreenHandler() != null && actionType == SlotActionType.PICKUP) { // -999 is code for "clicked outside the main window"
            ItemStack cursorStack = getScreenHandler().getCursorStack();
            if (cursorStack != null && IsSlotProtectedEvent.shouldBlockInteraction(slot, SlotActionType.THROW, cursorStack)) {
                ci.cancel();
                return;
            }
        }
        if (IsSlotProtectedEvent.shouldBlockInteraction(slot, actionType)) {
            ci.cancel();
            return;
        }
        if (actionType == SlotActionType.SWAP && 0 <= button && button < 9) {
            if (IsSlotProtectedEvent.shouldBlockInteraction(new Slot(playerInventory, button, 0, 0), actionType)) {
                ci.cancel();
            }
        }
    }


    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onAfterDrawSlot(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci, int i, int j, int k, Slot slot) {
        SlotRenderEvents.After event = new SlotRenderEvents.After(context, slot, mouseX, mouseY, delta);
        SlotRenderEvents.After.Companion.publish(event);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onBeforeDrawSlot(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci, int i, int j, int k, Slot slot) {
        SlotRenderEvents.Before event = new SlotRenderEvents.Before(context, slot, mouseX, mouseY, delta);
        SlotRenderEvents.Before.Companion.publish(event);
    }
}
