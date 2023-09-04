/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import moe.nea.firmament.util.unformattedString
import net.minecraft.text.Text

/**
 * Filter whether the user should see a chat message altogether. May or may not be called for every chat packet sent by
 * the server. When that quality is desired, consider [ProcessChatEvent] instead.
 */
data class AllowChatEvent(val text: Text) : FirmamentEvent.Cancellable() {
    val unformattedString = text.unformattedString

    companion object : FirmamentEventBus<AllowChatEvent>()
}
