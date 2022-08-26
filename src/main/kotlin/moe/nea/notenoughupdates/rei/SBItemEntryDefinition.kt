package moe.nea.notenoughupdates.rei

import io.github.moulberry.repo.data.NEUItem
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer
import me.shedaniel.rei.api.client.gui.widgets.Tooltip
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext
import me.shedaniel.rei.api.common.entry.EntrySerializer
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext
import me.shedaniel.rei.api.common.entry.type.EntryDefinition
import me.shedaniel.rei.api.common.entry.type.EntryType
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes
import moe.nea.notenoughupdates.rei.NEUReiPlugin.Companion.asItemEntry
import moe.nea.notenoughupdates.repo.ItemCache.asItemStack
import moe.nea.notenoughupdates.repo.ItemCache.getIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import net.minecraft.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.stream.Stream

object SBItemEntryDefinition : EntryDefinition<NEUItem> {
    override fun equals(o1: NEUItem?, o2: NEUItem?, context: ComparisonContext?): Boolean {
        return o1 === o2
    }

    override fun cheatsAs(entry: EntryStack<NEUItem>?, value: NEUItem?): ItemStack? {
        return value?.asItemStack()
    }

    override fun getValueType(): Class<NEUItem> = NEUItem::class.java
    override fun getType(): EntryType<NEUItem> =
        EntryType.deferred(NEUReiPlugin.SKYBLOCK_ITEM_TYPE_ID)

    override fun getRenderer(): EntryRenderer<NEUItem> = object : EntryRenderer<NEUItem> {
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

    override fun getSerializer(): EntrySerializer<NEUItem>? {
        return null
    }

    override fun getTagsFor(entry: EntryStack<NEUItem>?, value: NEUItem?): Stream<out TagKey<*>> {
        return Stream.empty()
    }

    override fun asFormattedText(entry: EntryStack<NEUItem>, value: NEUItem): Text {
        return VanillaEntryTypes.ITEM.definition.asFormattedText(entry.asItemEntry(), value.asItemStack())
    }

    override fun hash(entry: EntryStack<NEUItem>, value: NEUItem, context: ComparisonContext): Long {
        return System.identityHashCode(value) * 31L
    }

    override fun wildcard(entry: EntryStack<NEUItem>, value: NEUItem): NEUItem {
        return value
    }

    override fun normalize(entry: EntryStack<NEUItem>, value: NEUItem): NEUItem {
        return value
    }

    override fun copy(entry: EntryStack<NEUItem>?, value: NEUItem): NEUItem {
        return value
    }

    override fun isEmpty(entry: EntryStack<NEUItem>?, value: NEUItem?): Boolean {
        return false
    }

    override fun getIdentifier(entry: EntryStack<NEUItem>?, value: NEUItem): Identifier {
        return value.getIdentifier()
    }


}
