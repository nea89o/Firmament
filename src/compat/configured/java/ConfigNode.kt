package moe.nea.firmament.compat.configured

import com.mrcrayfish.configured.api.IConfigEntry
import com.mrcrayfish.configured.api.IConfigValue
import net.minecraft.text.Text
import moe.nea.firmament.gui.config.ManagedConfig

class ConfigNode(val config: ManagedConfig) : IConfigEntry {
    override fun getChildren(): List<IConfigEntry> {
        return config.allOptions.map {
            ConfigValueNode(it.value)
        }
    }

    override fun isRoot(): Boolean {
        return false
    }

    override fun isLeaf(): Boolean {
        return false
    }

    override fun getValue(): IConfigValue<*>? {
        return null
    }

    override fun getEntryName(): String {
        return config.translationKey
    }

    override fun getTooltip(): Text? {
        return null
    }

    override fun getTranslationKey(): String {
        return config.translationKey
    }

}
