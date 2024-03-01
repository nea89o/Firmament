/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.text.Text


fun ItemStack.appendLore(args: List<Text>) {
    if (args.isEmpty()) return
    val compoundTag = getOrCreateSubNbt("display")
    val loreList = compoundTag.getOrCreateList("Lore", NbtString.STRING_TYPE)
    for (arg in args) {
        loreList.add(NbtString.of(Text.Serialization.toJsonString(arg)))
    }
}

fun ItemStack.modifyLore(update: (List<Text>) -> List<Text>) {
    val compoundTag = getOrCreateSubNbt("display")
    val loreList = compoundTag.getOrCreateList("Lore", NbtString.STRING_TYPE)
    val parsed = loreList.map { Text.Serialization.fromJson(it.asString())!! }
    val updated = update(parsed)
    loreList.clear()
    loreList.addAll(updated.map { NbtString.of(Text.Serialization.toJsonString(it)) })
}


fun NbtCompound.getOrCreateList(label: String, tag: Byte): NbtList = getList(label, tag.toInt()).also {
    put(label, it)
}

fun NbtCompound.getOrCreateCompoundTag(label: String): NbtCompound = getCompound(label).also {
    put(label, it)
}
