/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.config

import io.github.notenoughupdates.moulconfig.observer.ObservableList
import io.github.notenoughupdates.moulconfig.xml.Bind
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import moe.nea.firmament.features.FeatureManager
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.MoulConfigUtils
import moe.nea.firmament.util.ScreenUtil.setScreenLater

object AllConfigsGui {

    val allConfigs
        get() = listOf(
            RepoManager.Config
        ) + FeatureManager.allFeatures.mapNotNull { it.config }

    fun <T> List<T>.toObservableList(): ObservableList<T> = ObservableList(this)

    class MainMapping(val allConfigs: List<ManagedConfig>) {
        @get:Bind("configs")
        val configs = allConfigs.map { EntryMapping(it) }.toObservableList()

        class EntryMapping(val config: ManagedConfig) {
            @Bind
            fun name() = Text.translatable("firmament.config.${config.name}").string

            @Bind
            fun openEditor() {
                config.showConfigEditor(MC.screen)
            }
        }
    }

    fun makeScreen(parent: Screen? = null): Screen {
        return MoulConfigUtils.loadScreen("config/main", MainMapping(allConfigs), parent)
    }

    fun showAllGuis() {
        setScreenLater(makeScreen())
    }
}
