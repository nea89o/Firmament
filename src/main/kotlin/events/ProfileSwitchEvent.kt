package moe.nea.firmament.events

import java.util.UUID

data class ProfileSwitchEvent(val oldProfile: UUID?, val newProfile: UUID?) : FirmamentEvent() {
	companion object : FirmamentEventBus<ProfileSwitchEvent>()
}
