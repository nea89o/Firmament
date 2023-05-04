package moe.nea.notenoughupdates.rei

import io.github.moulberry.repo.data.NEUItem
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer
import me.shedaniel.rei.api.client.gui.widgets.Tooltip
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes
import moe.nea.notenoughupdates.rei.NEUReiPlugin.Companion.asItemEntry
import net.minecraft.client.util.math.MatrixStack

object NEUItemEntryRenderer : EntryRenderer<NEUItem> {
    override fun render(
        entry: EntryStack<NEUItem>,
        matrices: MatrixStack,
        bounds: Rectangle,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        VanillaEntryTypes.ITEM.definition.renderer
            .render(
                entry.asItemEntry(),
                matrices, bounds, mouseX, mouseY, delta
            )
    }

    override fun getTooltip(entry: EntryStack<NEUItem>, tooltipContext: TooltipContext): Tooltip? {
        return VanillaEntryTypes.ITEM.definition.renderer
            .getTooltip(entry.asItemEntry(), tooltipContext)
    }

}
