package moe.nea.firmament.events

data class WorldMouseMoveEvent(val deltaX: Double, val deltaY: Double) : FirmamentEvent.Cancellable() {
	companion object : FirmamentEventBus<WorldMouseMoveEvent>()
}
