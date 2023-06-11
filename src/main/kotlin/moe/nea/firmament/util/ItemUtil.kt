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

package moe.nea.firmament.util

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.text.Text


fun ItemStack.appendLore(args: List<Text>) {
    val compoundTag = getOrCreateSubNbt("display")
    val loreList = compoundTag.getOrCreateList("Lore", NbtString.STRING_TYPE)
    for (arg in args) {
        loreList.add(NbtString.of(Text.Serializer.toJson(arg)))
    }
}

fun ItemStack.modifyLore(update: (List<Text>) -> List<Text>) {
    val compoundTag = getOrCreateSubNbt("display")
    val loreList = compoundTag.getOrCreateList("Lore", NbtString.STRING_TYPE)
    val parsed = loreList.map { Text.Serializer.fromJson(it.asString())!! }
    val updated = update(parsed)
    loreList.clear()
    loreList.addAll(updated.map { NbtString.of(Text.Serializer.toJson(it)) })
}


fun NbtCompound.getOrCreateList(label: String, tag: Byte): NbtList = getList(label, tag.toInt()).also {
    put(label, it)
}

fun NbtCompound.getOrCreateCompoundTag(label: String): NbtCompound = getCompound(label).also {
    put(label, it)
}
