package moe.nea.notenoughupdates.events

import net.minecraft.text.Text
import moe.nea.notenoughupdates.util.unformattedString

/**
 * This event gets published whenever the client receives a chat message from the server.
 */
data class ServerChatLineReceivedEvent(val text: Text) : NEUEvent.Cancellable() {
    companion object : NEUEventBus<ServerChatLineReceivedEvent>()

    val unformattedString = text.unformattedString
}