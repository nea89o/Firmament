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

import com.mojang.authlib.GameProfile;
import moe.nea.firmament.events.ServerChatLineReceivedEvent;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MessageHandler.class)
public class MixinMessageHandler {
    @Inject(method = "onChatMessage", cancellable = true, at = @At("HEAD"))
    public void onOnChatMessage(SignedMessage message, GameProfile sender, MessageType.Parameters params, CallbackInfo ci) {
        var decoratedText = params.applyChatDecoration(message.unsignedContent() != null ? message.unsignedContent() : message.getContent());
        var event = new ServerChatLineReceivedEvent(decoratedText);
        if (ServerChatLineReceivedEvent.Companion.publish(event).getCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    public void onOnGameMessage(Text message, boolean overlay, CallbackInfo ci) {
        if (!overlay) {
            var event = new ServerChatLineReceivedEvent(message);
            if (ServerChatLineReceivedEvent.Companion.publish(event).getCancelled()) {
                ci.cancel();
            }
        }
    }
}
