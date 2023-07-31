/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen

data class ScreenRenderPostEvent(
    val screen: Screen,
    val mouseX: Int,
    val mouseY: Int,
    val tickDelta: Float,
    val drawContext: DrawContext
) : FirmamentEvent() {
    companion object : FirmamentEventBus<ScreenRenderPostEvent>()
}
