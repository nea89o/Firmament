package moe.nea.firmament.events

class ClientInitEvent : FirmamentEvent() {
	companion object : FirmamentEventBus<ClientInitEvent>()
}
