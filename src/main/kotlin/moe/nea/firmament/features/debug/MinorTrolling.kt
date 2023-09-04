/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.debug

import moe.nea.firmament.events.ModifyChatEvent
import moe.nea.firmament.features.FirmamentFeature
import net.minecraft.text.Text


// In memorian Dulkir
object MinorTrolling : FirmamentFeature {
    override val identifier: String
        get() = "minor-trolling"

    val trollers = listOf("nea89o", "lrg89")
    val t = "From(?: \\[[^\\]]+])? ([^:]+): (.*)".toRegex()

    override fun onLoad() {
        ModifyChatEvent.subscribe {
            val m = t.matchEntire(it.unformattedString) ?: return@subscribe
            val (_, name, text) = m.groupValues
            if (name !in trollers) return@subscribe
            if (!text.startsWith("c:")) return@subscribe
            it.replaceWith = Text.literal(text.substring(2).replace("&", "§"))
        }
    }
}
