package moe.nea.firmament.rei

import io.github.moulberry.repo.data.NEUItem
import me.shedaniel.rei.api.common.entry.EntrySerializer
import me.shedaniel.rei.api.common.entry.EntryStack
import net.minecraft.nbt.NbtCompound
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.SkyblockId

object NEUItemEntrySerializer : EntrySerializer<NEUItem?> {
    const val SKYBLOCK_ID_ENTRY = "SKYBLOCK_ID"

    override fun supportSaving(): Boolean = true
    override fun supportReading(): Boolean = true

    override fun read(tag: NbtCompound): NEUItem? {
        return RepoManager.getNEUItem(SkyblockId(tag.getString(SKYBLOCK_ID_ENTRY)))
    }

    override fun save(entry: EntryStack<NEUItem?>, value: NEUItem?): NbtCompound {
        return NbtCompound().apply {
            putString(SKYBLOCK_ID_ENTRY, value?.skyblockItemId ?: "null")
        }
    }
}
