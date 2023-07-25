/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package moe.nea.firmament.mixins;

import moe.nea.firmament.events.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
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
        if (HandledScreenKeyPressedEvent.Companion.publish(new HandledScreenKeyPressedEvent((HandledScreen<?>) (Object) this, keyCode, scanCode, modifiers)).getCancelled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (ScreenClickEvent.Companion.publish(new ScreenClickEvent((HandledScreen<?>) (Object) this, mouseX, mouseY, button)).getCancelled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawForeground(Lnet/minecraft/client/gui/DrawContext;II)V", shift = At.Shift.AFTER))
    public void onAfterRenderForeground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        HandledScreenForegroundEvent.Companion.publish(new HandledScreenForegroundEvent((HandledScreen<?>) (Object) this, mouseX, mouseY, delta));
    }

    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At("HEAD"), cancellable = true)
    public void onMouseClickedSlot(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (IsSlotProtectedEvent.shouldBlockInteraction(slot)) {
            ci.cancel();
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
