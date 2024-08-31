package moe.nea.firmament.compat.configured

import com.mrcrayfish.configured.api.ConfigType
import com.mrcrayfish.configured.api.IConfigEntry
import com.mrcrayfish.configured.api.IModConfig
import com.mrcrayfish.configured.util.ConfigHelper
import java.nio.file.Path
import java.util.function.Consumer
import moe.nea.firmament.Firmament
import moe.nea.firmament.gui.config.ManagedConfig

class ConfigCategory(
    val category: ManagedConfig
) : BaseConfig() {

    override fun getRoot(): IConfigEntry {
        return ConfigNode(category)
    }

    override fun getTranslationKey(): String? {
        return category.translationKey
    }
}

abstract class BaseConfig : IModConfig {
    override fun update(p0: IConfigEntry) {
        ConfigHelper.getChangedValues(p0).forEach {
            it as ConfigValue
            it.saveValue()
        }
    }

    override fun getType(): ConfigType {
        return ConfigType.CLIENT
    }

    override fun getFileName(): String {
        return ""
    }

    override fun getModId(): String {
        return Firmament.MOD_ID
    }

    override fun loadWorldConfig(p0: Path?, p1: Consumer<IModConfig>?) {
    }

}
