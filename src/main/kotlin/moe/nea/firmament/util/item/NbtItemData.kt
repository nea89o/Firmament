package moe.nea.firmament.util.item

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtString
import net.minecraft.text.Text

val ItemStack.loreAccordingToNbt
    get() = getOrCreateSubNbt(ItemStack.DISPLAY_KEY).getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE.toInt())
        .map { Text.Serializer.fromJson((it as NbtString).asString()) }

val ItemStack.displayNameAccordingToNbt
    get() = getOrCreateSubNbt(ItemStack.DISPLAY_KEY).let {
        if (it.contains(ItemStack.NAME_KEY, NbtElement.STRING_TYPE.toInt()))
            Text.Serializer.fromJson(it.getString(ItemStack.NAME_KEY))
        else
            null
    }
