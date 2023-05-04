package moe.nea.notenoughupdates.rei

import io.github.moulberry.repo.data.NEUItem
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer
import me.shedaniel.rei.api.client.gui.widgets.Tooltip
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext
import me.shedaniel.rei.api.common.entry.EntryStack
import net.minecraft.client.util.math.MatrixStack
import moe.nea.notenoughupdates.rei.NEUReiPlugin.Companion.asItemEntry

object NEUItemEntryRenderer : EntryRenderer<NEUItem> {
    override fun render(
        entry: EntryStack<NEUItem>,
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

    override fun getTooltip(entry: EntryStack<NEUItem>, tooltipContext: TooltipContext): Tooltip? {
        return entry.asItemEntry().getTooltip(tooltipContext, false)
    }

}
