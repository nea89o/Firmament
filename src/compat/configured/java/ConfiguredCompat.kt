package moe.nea.firmament.compat.configured

import com.mrcrayfish.configured.api.IConfigEntry
import com.mrcrayfish.configured.api.IModConfig
import com.mrcrayfish.configured.api.IModConfigProvider
import com.mrcrayfish.configured.api.ModContext
import moe.nea.firmament.Firmament
import moe.nea.firmament.gui.config.AllConfigsGui

/**
 * Registered in `fabric.mod.json` at `custom.configured.providers`
 */
class ConfiguredCompat : IModConfigProvider {
    override fun getConfigurationsForMod(modContext: ModContext): Set<IModConfig> {
        if (modContext.modId != Firmament.MOD_ID) return emptySet()
        return buildSet {
            add(object : BaseConfig() {
                override fun getRoot(): IConfigEntry {
                    return BaseConfigNode
                }

                override fun getTranslationKey(): String? {
                    return "firmament.config.all-configs"
                }
            })
            AllConfigsGui.allConfigs.mapTo(this) { ConfigCategory(it) }
        }
    }
}
