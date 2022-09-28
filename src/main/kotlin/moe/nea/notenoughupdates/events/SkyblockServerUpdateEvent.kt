package moe.nea.notenoughupdates.events

import moe.nea.notenoughupdates.util.Locraw

/**
 * This event gets published whenever `/locraw` is queried and HyPixel returns a location different to the old one.
 *
 * **N.B.:** This event may get fired multiple times while on the server (for example, first to null, then to the
 * correct location).
 */
data class SkyblockServerUpdateEvent(val oldLocraw: Locraw?, val newLocraw: Locraw?) : NEUEvent() {
    companion object : NEUEventBus<SkyblockServerUpdateEvent>()
}
