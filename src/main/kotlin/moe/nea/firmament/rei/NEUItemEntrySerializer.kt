/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
        return SBItemStack(id, RepoManager.getNEUItem(id), count)
    }

    override fun save(entry: EntryStack<SBItemStack>, value: SBItemStack): NbtCompound {
        return NbtCompound().apply {
            putString(SKYBLOCK_ID_ENTRY, value.skyblockId.neuItem)
            putInt(SKYBLOCK_ITEM_COUNT, value.stackSize)
        }
    }
}
