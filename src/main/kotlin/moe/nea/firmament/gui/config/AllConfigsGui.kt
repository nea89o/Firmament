package moe.nea.firmament.gui.config

import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.CottonClientScreen
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.WListPanel
import io.github.cottonmc.cotton.gui.widget.data.Insets
import net.minecraft.text.Text
import moe.nea.firmament.features.FeatureManager
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.ScreenUtil.setScreenLater

object AllConfigsGui {

    fun showAllGuis() {
        val lwgd = LightweightGuiDescription()
        var screen: CottonClientScreen? = null
        lwgd.setRootPanel(WListPanel(
            listOf(
                RepoManager.Config
            ) + FeatureManager.allFeatures.mapNotNull { it.config }, ::WGridPanel
        ) { config, panel ->
            panel.insets = Insets.ROOT_PANEL
            panel.backgroundPainter = BackgroundPainter.VANILLA
            panel.add(WLabel(Text.translatable("firmament.config.${config.name}")), 0, 0, 10, 1)
            panel.add(WButton(Text.translatable("firmanent.config.edit")).also {
                it.setOnClick {
                    config.showConfigEditor(screen)
                }
            }, 0, 1, 10, 1)
            println("Panel size: ${panel.width} ${panel.height}")
        }.also {
            it.setListItemHeight(52)
            it.setSize(10 * 18 + 14 + 16, 300)
        })
        screen = CottonClientScreen(lwgd)
        setScreenLater(screen)
    }
}
