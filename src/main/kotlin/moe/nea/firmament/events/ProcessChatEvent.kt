/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import moe.nea.firmament.util.unformattedString
import net.minecraft.text.Text

/**
 * Behaves like [AllowChatEvent], but is triggered even when cancelled by other mods. Intended for data collection.
 * Make sure to subscribe to cancellable events as well when using.
 */
data class ProcessChatEvent(val text: Text, val wasExternallyCancelled: Boolean) : FirmamentEvent.Cancellable() {
    val unformattedString = text.unformattedString

    init {
        if (wasExternallyCancelled)
            cancelled = true
    }

    companion object : FirmamentEventBus<ProcessChatEvent>()
}
