/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import moe.nea.firmament.events.ClientChatLineReceivedEvent;
import moe.nea.firmament.features.fixes.Fixes;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class MixinChatHud {

    @ModifyExpressionValue(method = "render",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/hud/ChatHud;isChatFocused()Z"))
    public boolean onGetChatHud(boolean old) {
        return old || Fixes.INSTANCE.shouldPeekChat();
    }
    @ModifyExpressionValue(method = "getHeight",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/hud/ChatHud;isChatFocused()Z"))
    public boolean onGetChatHudHeight(boolean old) {
        return old || Fixes.INSTANCE.shouldPeekChat();
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"), method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V")
    public Text onAddMessage(Text message) {
        var event = new ClientChatLineReceivedEvent(message);
        if (ClientChatLineReceivedEvent.Companion.publish(event).getCancelled()) {
            return null;
        }
        return event.getReplaceWith();
    }

    @Inject(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", at = @At("HEAD"), cancellable = true)
    public void onAddMessage2(Text message, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo ci) {
        if (message == null) ci.cancel();
    }
}
