package moe.nea.firmament.rei

import io.github.moulberry.repo.data.NEUIngredient
import io.github.moulberry.repo.data.NEUItem
import java.util.stream.Stream
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer
import me.shedaniel.rei.api.common.entry.EntrySerializer
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext
import me.shedaniel.rei.api.common.entry.type.EntryDefinition
import me.shedaniel.rei.api.common.entry.type.EntryType
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import moe.nea.firmament.rei.FirmamentReiPlugin.Companion.asItemEntry
import moe.nea.firmament.repo.ItemCache.asItemStack
import moe.nea.firmament.repo.ItemCache.getIdentifier
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.SkyblockId

// TODO: allow stackable entries
object SBItemEntryDefinition : EntryDefinition<NEUItem> {
    override fun equals(o1: NEUItem?, o2: NEUItem?, context: ComparisonContext?): Boolean {
        return o1 === o2
    }

    override fun cheatsAs(entry: EntryStack<NEUItem>?, value: NEUItem?): ItemStack {
        return value.asItemStack()
    }

    override fun getValueType(): Class<NEUItem> = NEUItem::class.java
    override fun getType(): EntryType<NEUItem> = EntryType.deferred(FirmamentReiPlugin.SKYBLOCK_ITEM_TYPE_ID)

    override fun getRenderer(): EntryRenderer<NEUItem> = NEUItemEntryRenderer

    override fun getSerializer(): EntrySerializer<NEUItem?> {
        return NEUItemEntrySerializer
    }

    override fun getTagsFor(entry: EntryStack<NEUItem>?, value: NEUItem?): Stream<out TagKey<*>>? {
        return Stream.empty()
    }

    override fun asFormattedText(entry: EntryStack<NEUItem>, value: NEUItem): Text {
        return VanillaEntryTypes.ITEM.definition.asFormattedText(entry.asItemEntry(), value.asItemStack())
    }

    override fun hash(entry: EntryStack<NEUItem>, value: NEUItem?, context: ComparisonContext): Long {
        // Repo items are immutable, and get replaced entirely when loaded from disk
        return System.identityHashCode(value) * 31L
    }

    override fun wildcard(entry: EntryStack<NEUItem>?, value: NEUItem?): NEUItem? {
        return value
    }

    override fun normalize(entry: EntryStack<NEUItem>?, value: NEUItem?): NEUItem? {
        return value
    }

    override fun copy(entry: EntryStack<NEUItem>?, value: NEUItem?): NEUItem? {
        return value
    }

    override fun isEmpty(entry: EntryStack<NEUItem>?, value: NEUItem?): Boolean {
        return false
    }

    override fun getIdentifier(entry: EntryStack<NEUItem>?, value: NEUItem?): Identifier {
        return value?.getIdentifier() ?: Identifier.of("skyblockitem", "null")!!
    }

    fun getEntry(neuItem: NEUItem?): EntryStack<NEUItem> =
        EntryStack.of(this, neuItem)

    fun getEntry(skyblockId: SkyblockId?): EntryStack<NEUItem> =
        EntryStack.of(this, skyblockId?.let { RepoManager.getNEUItem(it) })

    fun getEntry(ingredient: NEUIngredient?): EntryStack<NEUItem> =
        getEntry(ingredient?.itemId?.let { SkyblockId(it) })


}
