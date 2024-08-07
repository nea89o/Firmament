

package moe.nea.firmament.util

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.text.Text
import moe.nea.firmament.util.item.loreAccordingToNbt


fun ItemStack.appendLore(args: List<Text>) {
    if (args.isEmpty()) return
    modifyLore {
        val loreList = loreAccordingToNbt.toMutableList()
        for (arg in args) {
            loreList.add(arg)
        }
        loreList
    }
}

fun ItemStack.modifyLore(update: (List<Text>) -> List<Text>) {
    val loreList = loreAccordingToNbt
    loreAccordingToNbt = update(loreList)
}
