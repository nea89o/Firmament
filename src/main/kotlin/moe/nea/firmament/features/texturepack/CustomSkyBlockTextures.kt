package moe.nea.firmament.features.texturepack

import net.minecraft.client.util.ModelIdentifier
import moe.nea.firmament.events.CustomItemModelEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.skyBlockId

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
            val id = it.itemStack.skyBlockId ?: return@subscribe
            it.overrideModel = ModelIdentifier("firmskyblock", id.identifier.path, "inventory")
        }
        TickEvent.subscribe {
            if (it.tickCount % TConfig.cacheDuration == 0)
                CustomItemModelEvent.clearCache()
        }
    }
}
