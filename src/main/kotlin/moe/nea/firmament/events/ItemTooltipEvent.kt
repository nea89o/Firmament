/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

data class ItemTooltipEvent(
    val stack: ItemStack, val context: TooltipContext, val lines: MutableList<Text>
) : FirmamentEvent() {
    companion object : FirmamentEventBus<ItemTooltipEvent>()
}
