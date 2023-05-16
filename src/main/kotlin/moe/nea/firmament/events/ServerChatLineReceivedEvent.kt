package moe.nea.firmament.events

import net.minecraft.text.Text
import moe.nea.firmament.util.unformattedString

/**
 * This event gets published whenever the client receives a chat message from the server.
 */
data class ServerChatLineReceivedEvent(val text: Text) : FirmamentEvent.Cancellable() {
    companion object : FirmamentEventBus<ServerChatLineReceivedEvent>()

    val unformattedString = text.unformattedString
}
