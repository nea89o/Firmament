/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import net.minecraft.item.ItemStack

sealed class PlayerInventoryUpdate : FirmamentEvent() {
    companion object : FirmamentEventBus<PlayerInventoryUpdate>()
    data class Single(val slot: Int, val stack: ItemStack) : PlayerInventoryUpdate()
    data class Multi(val contents: List<ItemStack>) : PlayerInventoryUpdate()

}
