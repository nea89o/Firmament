

package moe.nea.firmament.events

import net.minecraft.text.Text
import moe.nea.firmament.util.unformattedString

/**
 * Behaves like [AllowChatEvent], but is triggered even when cancelled by other mods. Intended for data collection.
 * Make sure to subscribe to cancellable events as well when using.
 */
data class ProcessChatEvent(val text: Text, val wasExternallyCancelled: Boolean) : FirmamentEvent.Cancellable() {
    val unformattedString = text.unformattedString

    val nameHeuristic: String? = run {
        val firstColon = unformattedString.indexOf(':')
        if (firstColon < 0) return@run null
        val firstSpace = unformattedString.lastIndexOf(' ', firstColon)
        unformattedString.substring(firstSpace + 1 until firstColon).takeIf { it.isNotEmpty() }
    }

    init {
        if (wasExternallyCancelled)
            cancelled = true
    }

    companion object : FirmamentEventBus<ProcessChatEvent>()
}
