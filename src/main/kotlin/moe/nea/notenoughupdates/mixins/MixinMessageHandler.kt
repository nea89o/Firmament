package moe.nea.notenoughupdates.mixins

import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import net.minecraft.client.network.message.MessageHandler
import net.minecraft.network.message.MessageType
import net.minecraft.network.message.SignedMessage
import net.minecraft.text.Text
import moe.nea.notenoughupdates.events.ServerChatLineReceivedEvent

@Mixin(MessageHandler::class)
class MixinMessageHandler {
    @Inject(method = ["onChatMessage"], at = [At("HEAD")], cancellable = true)
    fun onOnChatMessage(message: SignedMessage, params: MessageType.Parameters, ci: CallbackInfo) {
        val decoratedText = params.applyChatDecoration(message.unsignedContent.orElse(message.content))
        val event = ServerChatLineReceivedEvent(decoratedText)
        if (ServerChatLineReceivedEvent.publish(event).cancelled) {
            ci.cancel()
        }
    }

    @Inject(method = ["onGameMessage"], at = [At("HEAD")], cancellable = true)
    fun onOnGameMessage(message: Text, overlay: Boolean, ci: CallbackInfo) {
        if (!overlay) {
            val event = ServerChatLineReceivedEvent(message)
            if (ServerChatLineReceivedEvent.publish(event).cancelled) {
                ci.cancel()
            }
        }
    }
}
