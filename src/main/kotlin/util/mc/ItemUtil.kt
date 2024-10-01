package moe.nea.firmament.util.mc

import net.minecraft.item.ItemStack
import net.minecraft.text.Text

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
