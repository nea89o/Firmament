package moe.nea.notenoughupdates.util

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

fun ItemStack.appendLore(args: List<Component>) {
    val compoundTag = getOrCreateTagElement("display")
    val loreList = compoundTag.getOrCreateList("Lore", StringTag.TAG_STRING)
    for (arg in args) {
        loreList.add(StringTag.valueOf(Component.Serializer.toJson(arg)))
    }
}

fun CompoundTag.getOrCreateList(label: String, tag: Byte): ListTag = getList(label, tag.toInt()).also {
    put(label, it)
}

fun CompoundTag.getOrCreateCompoundTag(label: String): CompoundTag = getCompound(label).also {
    put(label, it)
}
