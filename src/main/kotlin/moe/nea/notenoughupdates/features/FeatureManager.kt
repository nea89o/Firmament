package moe.nea.notenoughupdates.features

import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import moe.nea.notenoughupdates.NotEnoughUpdates
import moe.nea.notenoughupdates.features.world.FairySouls
import moe.nea.notenoughupdates.util.config.ConfigHolder

object FeatureManager : ConfigHolder<FeatureManager.Config>(serializer(), "features", ::Config) {
    @Serializable
    data class Config(
        val enabledFeatures: MutableMap<String, Boolean> = mutableMapOf()
    )

    private val features = mutableMapOf<String, NEUFeature>()

    private var hasAutoloaded = false

    init {
        autoload()
    }

    fun autoload() {
        synchronized(this) {
            if (hasAutoloaded) return
            loadFeature(FairySouls)
            hasAutoloaded = true
        }
    }

    fun loadFeature(feature: NEUFeature) {
        synchronized(features) {
            if (feature.identifier in features) {
                NotEnoughUpdates.logger.error("Double registering feature ${feature.identifier}. Ignoring second instance $feature")
                return
            }
            features[feature.identifier] = feature
            feature.onLoad()
        }
    }

    fun isEnabled(identifier: String): Boolean? =
        config.enabledFeatures[identifier]


    fun setEnabled(identifier: String, value: Boolean) {
        config.enabledFeatures[identifier] = value
        markDirty()
    }

}
