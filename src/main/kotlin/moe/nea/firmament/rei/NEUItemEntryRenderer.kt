/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.rei

import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer
import me.shedaniel.rei.api.client.gui.widgets.Tooltip
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext
import me.shedaniel.rei.api.common.entry.EntryStack
import net.minecraft.client.gui.DrawContext
import moe.nea.firmament.rei.FirmamentReiPlugin.Companion.asItemEntry

object NEUItemEntryRenderer : EntryRenderer<SBItemStack> {
    override fun render(
        entry: EntryStack<SBItemStack>,
        context: DrawContext,
        bounds: Rectangle,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        entry.asItemEntry().render(context, bounds, mouseX, mouseY, delta)
    }

    override fun getTooltip(entry: EntryStack<SBItemStack>, tooltipContext: TooltipContext): Tooltip? {
        return entry.asItemEntry().getTooltip(tooltipContext, false)
    }

}
