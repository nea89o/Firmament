package moe.nea.firmament.events

import moe.nea.firmament.annotations.Subscribe

class ComponentsLoadedEvent : FirmamentEvent() {
	companion object : FirmamentEventBus<ComponentsLoadedEvent>() {
		var generation = -1

		@Subscribe
		fun onComponentsLoaded(event: ComponentsLoadedEvent) {
			generation++
		}
	}
}
