package moe.nea.firmament.events

import io.github.moulberry.repo.NEURepository

data class ReloadRegistrationEvent(val repo: NEURepository) : FirmamentEvent() {
    companion object : FirmamentEventBus<ReloadRegistrationEvent>()
}
