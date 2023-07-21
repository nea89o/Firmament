package moe.nea.firmament.jarvis

import moe.nea.jarvis.api.JarvisConfigOption
import moe.nea.jarvis.api.JarvisPlugin
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.features.FeatureManager
import moe.nea.firmament.repo.RepoManager

class JarvisIntegration : JarvisPlugin {
    override fun getModId(): String =
        Firmament.MOD_ID

    override fun getAllConfigOptions(): List<JarvisConfigOption> {
        val configs = listOf(
            RepoManager.Config
        ) + FeatureManager.allFeatures.mapNotNull { it.config }
        return configs.flatMap { config ->
            config.sortedOptions.map {
                object : JarvisConfigOption {
                    override fun title(): Text {
                        return it.labelText
                    }

                    override fun description(): List<Text> {
                        return emptyList()
                    }

                    override fun jumpTo(parentScreen: Screen?): Screen {
                        return config.getConfigEditor(parentScreen)
                    }
                }
            }
        }
    }
}
