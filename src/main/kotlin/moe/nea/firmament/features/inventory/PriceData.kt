/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.inventory

import net.minecraft.text.Text
import moe.nea.firmament.events.ItemTooltipEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.repo.HypixelStaticData
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.skyBlockId

object PriceData : FirmamentFeature {
    override val identifier: String
        get() = "price-data"

    object TConfig : ManagedConfig(identifier) {
        val tooltipEnabled by toggle("enable-always") { true }
        val enableKeybinding by keyBindingWithDefaultUnbound("enable-keybind")
    }

    override val config get() = TConfig

    override fun onLoad() {
        ItemTooltipEvent.subscribe {
            if (!TConfig.tooltipEnabled && !TConfig.enableKeybinding.isPressed()) {
                return@subscribe
            }
            val sbId = it.stack.skyBlockId
            val bazaarData = HypixelStaticData.bazaarData[sbId]
            val lowestBin = HypixelStaticData.lowestBin[sbId]
            if (bazaarData != null) {
                it.lines.add(Text.literal(""))
                it.lines.add(
                    Text.stringifiedTranslatable("firmament.tooltip.bazaar.sell-order", FirmFormatters.formatCurrency(bazaarData.quickStatus.sellPrice, 1))
                )
                it.lines.add(
                    Text.stringifiedTranslatable("firmament.tooltip.bazaar.buy-order", FirmFormatters.formatCurrency(bazaarData.quickStatus.buyPrice, 1))
                )
            } else if (lowestBin != null) {
                it.lines.add(Text.literal(""))
                it.lines.add(
                    Text.stringifiedTranslatable("firmament.tooltip.ah.lowestbin", FirmFormatters.formatCurrency(lowestBin, 1))
                )
            }
        }
    }
}
