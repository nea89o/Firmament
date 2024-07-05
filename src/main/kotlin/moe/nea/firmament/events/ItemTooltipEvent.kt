/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import net.minecraft.item.Item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text

data class ItemTooltipEvent(
    val stack: ItemStack, val context: TooltipContext, val type: TooltipType, val lines: MutableList<Text>
) : FirmamentEvent() {
    companion object : FirmamentEventBus<ItemTooltipEvent>()
}
