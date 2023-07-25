package moe.nea.firmament.features.texturepack

import net.minecraft.client.util.ModelIdentifier
import moe.nea.firmament.events.CustomItemModelEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.extraAttributes

object CustomSkyBlockTextures : FirmamentFeature {
    override val identifier: String
        get() = "custom-skyblock-textures"

    object TConfig : ManagedConfig(identifier) {
        val enabled by toggle("enabled") { true }
        val cacheDuration by integer("cache-duration", 0, 20) { 1 }
    }

    override val config: ManagedConfig
        get() = TConfig

    override fun onLoad() {
        CustomItemModelEvent.subscribe {
            if (!TConfig.enabled) return@subscribe
            val extra = it.itemStack.extraAttributes
            val id = extra.getString("id")
            if (id.isNotBlank())
                it.overrideModel = ModelIdentifier("firmskyblock", id.lowercase(), "inventory")
        }
        TickEvent.subscribe {
            if (it.tickCount % TConfig.cacheDuration == 0)
                CustomItemModelEvent.clearCache()
        }
    }
}
