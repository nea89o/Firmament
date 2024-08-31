package moe.nea.firmament.compat.configured

import com.mrcrayfish.configured.api.IConfigEntry
import com.mrcrayfish.configured.api.IConfigValue
import net.minecraft.text.Text
import moe.nea.firmament.gui.config.ManagedOption

class ConfigValueNode(val option: ManagedOption<*>) : IConfigEntry {
    override fun getChildren(): List<IConfigEntry> {
        return listOf()
    }

    override fun isRoot(): Boolean {
        return false
    }

    override fun isLeaf(): Boolean {
        return true
    }

    val value = ConfigValue(option)
    override fun getValue(): IConfigValue<*>? {
        return value
    }

    override fun getEntryName(): String {
        return option.propertyName
    }

    override fun getTooltip(): Text? {
        return null
    }

    override fun getTranslationKey(): String? {
        return option.rawLabelText
    }
}
