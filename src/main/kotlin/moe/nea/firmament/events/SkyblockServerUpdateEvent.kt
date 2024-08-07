

package moe.nea.firmament.events

import moe.nea.firmament.util.Locraw

/**
 * This event gets published whenever `/locraw` is queried and HyPixel returns a location different to the old one.
 *
 * **N.B.:** This event may get fired multiple times while on the server (for example, first to null, then to the
 * correct location).
 */
data class SkyblockServerUpdateEvent(val oldLocraw: Locraw?, val newLocraw: Locraw?) : FirmamentEvent() {
    companion object : FirmamentEventBus<SkyblockServerUpdateEvent>()
}
