

package moe.nea.firmament.features

import moe.nea.firmament.events.subscription.SubscriptionOwner
import moe.nea.firmament.gui.config.ManagedConfig

// TODO: remove this entire feature system and revamp config
interface FirmamentFeature : SubscriptionOwner {
    val identifier: String
    val defaultEnabled: Boolean
        get() = true
    var isEnabled: Boolean
        get() = FeatureManager.isEnabled(identifier) ?: defaultEnabled
        set(value) {
            FeatureManager.setEnabled(identifier, value)
        }
    override val delegateFeature: FirmamentFeature
        get() = this
    val config: ManagedConfig? get() = null
    fun onLoad() {}

}
