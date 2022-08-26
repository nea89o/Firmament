package moe.nea.notenoughupdates.util

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

fun NbtCompound.getOrCreateList(label: String, tag: Byte): NbtList = getList(label, tag.toInt()).also {
    put(label, it)
}

fun NbtCompound.getOrCreateCompoundTag(label: String): NbtCompound = getCompound(label).also {
    put(label, it)
}
