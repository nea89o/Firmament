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
