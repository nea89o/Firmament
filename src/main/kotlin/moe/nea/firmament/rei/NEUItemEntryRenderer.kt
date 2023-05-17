package moe.nea.firmament.rei

import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer
import me.shedaniel.rei.api.client.gui.widgets.Tooltip
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext
import me.shedaniel.rei.api.common.entry.EntryStack
import net.minecraft.client.util.math.MatrixStack
import moe.nea.firmament.rei.FirmamentReiPlugin.Companion.asItemEntry

object NEUItemEntryRenderer : EntryRenderer<SBItemStack> {
    override fun render(
        entry: EntryStack<SBItemStack>,
        matrices: MatrixStack,
        bounds: Rectangle,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        matrices.push()
        matrices.translate(0F, 0F, 100F)
        entry.asItemEntry().render(matrices, bounds, mouseX, mouseY, delta)
        matrices.pop()
    }

    override fun getTooltip(entry: EntryStack<SBItemStack>, tooltipContext: TooltipContext): Tooltip? {
        return entry.asItemEntry().getTooltip(tooltipContext, false)
    }

}
