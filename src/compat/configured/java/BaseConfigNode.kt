package moe.nea.firmament.compat.configured

import com.mrcrayfish.configured.api.IConfigEntry
import com.mrcrayfish.configured.api.IConfigValue
import net.minecraft.text.Text
import moe.nea.firmament.gui.config.AllConfigsGui

object BaseConfigNode : IConfigEntry {
    override fun getChildren(): List<IConfigEntry> {
        return AllConfigsGui.allConfigs.map {
            ConfigNode(it)
        }
    }

    override fun isRoot(): Boolean {
        return true
    }

    override fun isLeaf(): Boolean {
        return false
    }

    override fun getValue(): IConfigValue<*>? {
        return null
    }

    override fun getEntryName(): String {
        return "Firmament"
    }

    override fun getTooltip(): Text? {
        return null
    }

    override fun getTranslationKey(): String? {
        return null
    }

}
