package moe.nea.firmament.gui

import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WItem
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import moe.nea.firmament.util.MC

class WTitledItem(val stack: ItemStack, val countString: Text = Text.empty()) : WItem(stack) {
    override fun paint(context: DrawContext, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        BackgroundPainter.SLOT.paintBackground(context, x, y, this)
        super.paint(context, x, y, mouseX, mouseY)
        context.matrices.push()
        context.matrices.translate(0F, 0F, 200F)
        context.drawText(MC.font, countString, x + 19 - 2 - MC.font.getWidth(countString), y + 6 + 3, 0xFFFFFF, true)
        context.matrices.push()
    }

    override fun addTooltip(tooltip: TooltipBuilder) {
        tooltip.add(*stack.getTooltip(null, TooltipContext.BASIC).toTypedArray())
    }

}
