/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.debug

import net.minecraft.text.Text
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ModifyChatEvent
import moe.nea.firmament.features.FirmamentFeature


// In memorian Dulkir
object MinorTrolling : FirmamentFeature {
    override val identifier: String
        get() = "minor-trolling"

    val trollers = listOf("nea89o", "lrg89")
    val t = "From(?: \\[[^\\]]+])? ([^:]+): (.*)".toRegex()

    @Subscribe
    fun onTroll(it: ModifyChatEvent) {
        val m = t.matchEntire(it.unformattedString) ?: return
        val (_, name, text) = m.groupValues
        if (name !in trollers) return
        if (!text.startsWith("c:")) return
        it.replaceWith = Text.literal(text.substring(2).replace("&", "§"))
    }
}
