/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package moe.nea.firmament.gui.config

import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.CottonClientScreen
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.WBox
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WScrollPanel
import io.github.cottonmc.cotton.gui.widget.data.Axis
import io.github.cottonmc.cotton.gui.widget.data.Insets
import io.ktor.http.*
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import moe.nea.firmament.features.FeatureManager
import moe.nea.firmament.gui.WFixedPanel
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.ScreenUtil.setScreenLater

object AllConfigsGui {

    fun makeScreen(parent: Screen? = null): CottonClientScreen {
        val lwgd = LightweightGuiDescription()
        var screen: CottonClientScreen? = null
        val configs = listOf(
            RepoManager.Config
        ) + FeatureManager.allFeatures.mapNotNull { it.config }
        val box = WBox(Axis.VERTICAL)
        configs.forEach { config ->
            val panel = WGridPanel()
            panel.insets = Insets.ROOT_PANEL
            panel.backgroundPainter = BackgroundPainter.VANILLA
            panel.add(WLabel(Text.translatable("firmament.config.${config.name}")), 0, 0, 10, 1)
            panel.add(WButton(Text.translatable("firmanent.config.edit")).also {
                it.setOnClick {
                    config.showConfigEditor(screen)
                }
            }, 0, 1, 10, 1)
            box.add(WFixedPanel(panel))
        }
        box.insets = Insets.ROOT_PANEL
        lwgd.setRootPanel(WScrollPanel((box)).also {
            box.layout()
            it.setSize(box.width + 8, MC.window.scaledHeight / 2)
        })
        screen = object :  CottonClientScreen(lwgd) {
            override fun close() {
                MC.screen = parent
            }
        }
        return screen
    }

    fun showAllGuis() {
        setScreenLater(makeScreen())
    }
}
