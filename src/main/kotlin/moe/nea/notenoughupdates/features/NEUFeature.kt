package moe.nea.notenoughupdates.features

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

    fun onLoad()

}
