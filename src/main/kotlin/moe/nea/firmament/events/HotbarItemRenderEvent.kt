/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack

data class HotbarItemRenderEvent(
    val item: ItemStack,
    val context: DrawContext,
    val x: Int,
    val y: Int,
    val tickDelta: Float,
) : FirmamentEvent() {
    companion object : FirmamentEventBus<HotbarItemRenderEvent>()
}
