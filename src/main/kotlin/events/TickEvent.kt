

package moe.nea.firmament.events

data class TickEvent(val tickCount: Int) : FirmamentEvent() {
    companion object : FirmamentEventBus<TickEvent>()
}
