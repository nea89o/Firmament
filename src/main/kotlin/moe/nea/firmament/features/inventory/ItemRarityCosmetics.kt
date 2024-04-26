/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.inventory

import java.awt.Color
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import moe.nea.firmament.events.HotbarItemRenderEvent
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.item.loreAccordingToNbt
import moe.nea.firmament.util.unformattedString

object ItemRarityCosmetics : FirmamentFeature {
    override val identifier: String
        get() = "item-rarity-cosmetics"

    object TConfig : ManagedConfig(identifier) {
        val showItemRarityBackground by toggle("background") { false }
        val showItemRarityInHotbar by toggle("background-hotbar") { false }
    }

    override val config: ManagedConfig
        get() = TConfig

    private val rarityToColor = mapOf(
        "UNCOMMON" to Formatting.GREEN,
        "COMMON" to Formatting.WHITE,
        "RARE" to Formatting.DARK_BLUE,
        "EPIC" to Formatting.DARK_PURPLE,
        "LEGENDARY" to Formatting.GOLD,
        "LEGENJERRY" to Formatting.GOLD,
        "MYTHIC" to Formatting.LIGHT_PURPLE,
        "DIVINE" to Formatting.BLUE,
        "SPECIAL" to Formatting.DARK_RED,
        "SUPREME" to Formatting.DARK_RED,
    ).mapValues {
        val c = Color(it.value.colorValue!!)
        Triple(c.red / 255F, c.green / 255F, c.blue / 255F)
    }
    private val ItemStack.skyblockLoreRarityColor: Triple<Float, Float, Float>?
        get() {
            val entry = loreAccordingToNbt.lastOrNull()?.unformattedString ?: ""
            return rarityToColor.entries.find { (k, v) -> k in entry }?.value
        }


    fun drawItemStackRarity(drawContext: DrawContext, x: Int, y: Int, item: ItemStack) {
        val (r, g, b) = item.skyblockLoreRarityColor ?: return
        drawContext.drawSprite(
            x, y,
            0,
            16, 16,
            MC.guiAtlasManager.getSprite(Identifier("firmament:item_rarity_background")),
            r, g, b, 1F
        )
    }

    override fun onLoad() {
        HotbarItemRenderEvent.subscribe {
            if (!TConfig.showItemRarityInHotbar) return@subscribe
            val stack = it.item
            drawItemStackRarity(it.context, it.x, it.y, stack)
        }
        SlotRenderEvents.Before.subscribe {
            if (!TConfig.showItemRarityBackground) return@subscribe
            val stack = it.slot.stack ?: return@subscribe
            drawItemStackRarity(it.context, it.slot.x, it.slot.y, stack)
        }
    }
}
