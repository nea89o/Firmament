package moe.nea.firmament.events

class WorldReadyEvent : FirmamentEvent() {
	companion object : FirmamentEventBus<WorldReadyEvent>()
//	class FullyLoaded : FirmamentEvent() {
//		companion object : FirmamentEventBus<FullyLoaded>() {
//			 TODO: check WorldLoadingState
//		}
//	}
}
