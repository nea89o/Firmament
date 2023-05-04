package moe.nea.notenoughupdates.features

import moe.nea.notenoughupdates.util.config.ManagedConfig

interface NEUFeature {
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