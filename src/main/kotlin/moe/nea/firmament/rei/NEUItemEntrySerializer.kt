/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.rei

import me.shedaniel.rei.api.common.entry.EntrySerializer
import me.shedaniel.rei.api.common.entry.EntryStack
import net.minecraft.nbt.NbtCompound
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.SkyblockId

object NEUItemEntrySerializer : EntrySerializer<SBItemStack> {
    const val SKYBLOCK_ID_ENTRY = "SKYBLOCK_ID"
    const val SKYBLOCK_ITEM_COUNT = "SKYBLOCK_ITEM_COUNT"

    override fun supportSaving(): Boolean = true
    override fun supportReading(): Boolean = true

    override fun read(tag: NbtCompound): SBItemStack {
        val id = SkyblockId(tag.getString(SKYBLOCK_ID_ENTRY))
        val count = if (tag.contains(SKYBLOCK_ITEM_COUNT)) tag.getInt(SKYBLOCK_ITEM_COUNT) else 1
        return SBItemStack(id, count)
    }

    override fun save(entry: EntryStack<SBItemStack>, value: SBItemStack): NbtCompound {
        return NbtCompound().apply {
            putString(SKYBLOCK_ID_ENTRY, value.skyblockId.neuItem)
            putInt(SKYBLOCK_ITEM_COUNT, value.stackSize)
        }
    }
}
