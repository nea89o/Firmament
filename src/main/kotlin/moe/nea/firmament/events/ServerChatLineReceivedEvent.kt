/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import net.minecraft.text.Text
import moe.nea.firmament.util.unformattedString

/**
 * This event gets published whenever the client receives a chat message from the server.
 * This event is cancellable, but should not get cancelled. Use [ClientChatLineReceivedEvent] for that instead. */
data class ServerChatLineReceivedEvent(val text: Text) : FirmamentEvent.Cancellable() {
    val unformattedString = text.unformattedString

    companion object : FirmamentEventBus<ServerChatLineReceivedEvent>()
}
