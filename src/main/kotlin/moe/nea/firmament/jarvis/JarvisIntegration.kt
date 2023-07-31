/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.jarvis

import moe.nea.jarvis.api.Jarvis
import moe.nea.jarvis.api.JarvisConfigOption
import moe.nea.jarvis.api.JarvisHud
import moe.nea.jarvis.api.JarvisPlugin
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.features.FeatureManager
import moe.nea.firmament.gui.config.HudMeta
import moe.nea.firmament.gui.config.HudMetaHandler
import moe.nea.firmament.repo.RepoManager

class JarvisIntegration : JarvisPlugin {
    override fun getModId(): String =
        Firmament.MOD_ID

    companion object {
        lateinit var jarvis: Jarvis
    }

    override fun onInitialize(jarvis: Jarvis) {
        Companion.jarvis = jarvis
    }

    val configs
        get() = listOf(
            RepoManager.Config
        ) + FeatureManager.allFeatures.mapNotNull { it.config }


    override fun getAllHuds(): List<JarvisHud> {
        return configs.flatMap { config ->
            config.sortedOptions.mapNotNull { if (it.handler is HudMetaHandler) it.value as HudMeta else null }
        }
    }

    override fun getAllConfigOptions(): List<JarvisConfigOption> {
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
