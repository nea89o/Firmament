package moe.nea.notenoughupdates.features

import kotlinx.serialization.serializer
import moe.nea.notenoughupdates.NotEnoughUpdates
import moe.nea.notenoughupdates.features.world.FairySouls
import moe.nea.notenoughupdates.util.ConfigHolder

object FeatureManager : ConfigHolder<FeatureManager.Config>(serializer(), "features", ::Config) {
    data class Config(
        val enabledFeatures: MutableMap<String, Boolean> = mutableMapOf()
    )

    private val features = mutableMapOf<String, NEUFeature>()

    fun autoload() {
        loadFeature(FairySouls)
    }

    fun loadFeature(feature: NEUFeature) {
        if (feature.identifier in features) {
            NotEnoughUpdates.logger.error("Double registering feature ${feature.identifier}. Ignoring second instance $feature")
            return
        }
        features[feature.identifier] = feature
    }

    fun isEnabled(identifier: String): Boolean? =
        config.enabledFeatures[identifier]


    fun setEnabled(identifier: String, value: Boolean) {
        config.enabledFeatures[identifier] = value
        markDirty()
    }

}
