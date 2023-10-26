/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui

import com.mojang.blaze3d.systems.RenderSystem
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WWidget
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import moe.nea.firmament.util.MC

open class WTitledItem(var stack: ItemStack, val countString: Text = Text.empty()) : WWidget() {
    var backgroundPainter:BackgroundPainter = BackgroundPainter.SLOT
    override fun canResize(): Boolean = true
    override fun paint(context: DrawContext, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        backgroundPainter.paintBackground(context, x, y, this)
        context.matrices.push()
        context.matrices.translate(x.toFloat(), y.toFloat(), 0F)
        context.matrices.scale(width / 18F, height / 18F, 1F)
        RenderSystem.enableDepthTest()
        context.drawItemWithoutEntity(stack, 18 / 2 - 8, 18 / 2 - 8)
        context.matrices.translate(0F, 0F, 200F)
        context.drawText(MC.font, countString, 19 - 2 - MC.font.getWidth(countString), 6 + 3, 0xFFFFFF, true)
        context.matrices.pop()
    }

    override fun addTooltip(tooltip: TooltipBuilder) {
        tooltip.add(*stack.getTooltip(null, TooltipContext.BASIC).toTypedArray())
    }

}
