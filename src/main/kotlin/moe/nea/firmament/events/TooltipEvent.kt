/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

data class TooltipEvent(
    val itemStack: ItemStack,
    val tooltip: Tooltip,
    val tooltipContext: Item.TooltipContext,
    val player: PlayerEntity?
) : FirmamentEvent() {
    companion object : FirmamentEventBus<TooltipEvent>()
}
