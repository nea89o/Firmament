
package moe.nea.firmament.events

import moe.nea.firmament.features.FirmamentFeature

data class FeaturesInitializedEvent(val features: List<FirmamentFeature>) : FirmamentEvent() {
    companion object : FirmamentEventBus<FeaturesInitializedEvent>()
}
