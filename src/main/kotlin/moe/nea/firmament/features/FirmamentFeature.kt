package moe.nea.firmament.features

import moe.nea.firmament.gui.config.ManagedConfig

interface FirmamentFeature {
    val name: String
    val identifier: String
    val defaultEnabled: Boolean
        get() = true
    var isEnabled: Boolean
        get() = FeatureManager.isEnabled(identifier) ?: defaultEnabled
        set(value) {
            FeatureManager.setEnabled(identifier, value)
        }
    val config: ManagedConfig? get() = null
    fun onLoad()

}
