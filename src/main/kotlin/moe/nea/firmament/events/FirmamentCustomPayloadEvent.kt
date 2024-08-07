
package moe.nea.firmament.events

import moe.nea.firmament.apis.ingame.FirmamentCustomPayload

data class FirmamentCustomPayloadEvent(
    val payload: FirmamentCustomPayload
) : FirmamentEvent() {
    companion object : FirmamentEventBus<FirmamentCustomPayloadEvent>()
}
