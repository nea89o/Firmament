/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events.registration

import moe.nea.firmament.events.AllowChatEvent
import moe.nea.firmament.events.ModifyChatEvent
import moe.nea.firmament.events.ProcessChatEvent
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.text.Text

private var lastReceivedMessage: Text? = null

fun registerFirmamentChatEvents() {
    ClientReceiveMessageEvents.ALLOW_CHAT.register(ClientReceiveMessageEvents.AllowChat { message, signedMessage, sender, params, receptionTimestamp ->
        lastReceivedMessage = message
        !ProcessChatEvent.publish(ProcessChatEvent(message, false)).cancelled
                && !AllowChatEvent.publish(AllowChatEvent(message)).cancelled
    })
    ClientReceiveMessageEvents.ALLOW_GAME.register(ClientReceiveMessageEvents.AllowGame { message, overlay ->
        lastReceivedMessage = message
        overlay || (!ProcessChatEvent.publish(ProcessChatEvent(message, false)).cancelled &&
                !AllowChatEvent.publish(AllowChatEvent(message)).cancelled)
    })
    ClientReceiveMessageEvents.MODIFY_GAME.register(ClientReceiveMessageEvents.ModifyGame { message, overlay ->
        if (overlay) message
        else ModifyChatEvent.publish(ModifyChatEvent(message)).replaceWith
    })
    ClientReceiveMessageEvents.GAME_CANCELED.register(ClientReceiveMessageEvents.GameCanceled { message, overlay ->
        if (!overlay && lastReceivedMessage !== message) {
            ProcessChatEvent.publish(ProcessChatEvent(message, true))
        }
    })
    ClientReceiveMessageEvents.CHAT_CANCELED.register(ClientReceiveMessageEvents.ChatCanceled { message, signedMessage, sender, params, receptionTimestamp ->
        if (lastReceivedMessage !== message) {
            ProcessChatEvent.publish(ProcessChatEvent(message, true))
        }
    })
}
