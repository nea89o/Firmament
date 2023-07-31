/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.config

import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.CottonClientScreen
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.WBox
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WClippedPanel
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WPanel
import io.github.cottonmc.cotton.gui.widget.WPanelWithInsets
import io.github.cottonmc.cotton.gui.widget.WScrollPanel
import io.github.cottonmc.cotton.gui.widget.data.Axis
import io.github.cottonmc.cotton.gui.widget.data.Insets
import io.ktor.http.*
import kotlin.streams.asSequence
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import moe.nea.firmament.features.FeatureManager
import moe.nea.firmament.gui.WFixedPanel
import moe.nea.firmament.gui.WSplitPanel
import moe.nea.firmament.gui.WTightScrollPanel
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
            val panel = WSplitPanel(
                WLabel(Text.translatable("firmament.config.${config.name}")),
                WButton(Text.translatable("firmanent.config.edit")).also {
                    it.setOnClick {
                        config.showConfigEditor(screen)
                    }
                    it.setSize(40, 18)
                }
            )
            panel.insets = Insets.ROOT_PANEL
            panel.backgroundPainter = BackgroundPainter.VANILLA
            box.add((panel))
        }
        box.streamChildren().asSequence()
            .forEach { it.setSize(380, 0) }
        lwgd.setRootPanel(WBox(
            Axis.VERTICAL
        ).also {
            it.insets = Insets.ROOT_PANEL
            box.layout()
            it.add(WFixedPanel((WScrollPanel((box)).also {
                it.setSize(400, 300)
            })))
            it.setSize(400, 300)
        })

        screen = object : CottonClientScreen(lwgd) {
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
