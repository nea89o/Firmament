package moe.nea.firmament.events

import net.minecraft.text.Text
import moe.nea.firmament.util.unformattedString

/**
 * Published just before a message is added to the chat gui. Cancelling only prevents rendering, not logging to the
 * console.
 */
data class ClientChatLineReceivedEvent(val text: Text) : FirmamentEvent.Cancellable() {
    val unformattedString = text.unformattedString

    companion object : FirmamentEventBus<ClientChatLineReceivedEvent>()
}
