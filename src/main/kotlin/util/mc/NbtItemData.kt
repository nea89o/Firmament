package moe.nea.firmament.util.mc

import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

var ItemStack.loreAccordingToNbt: List<Text>
	get() = get(DataComponentTypes.LORE)?.lines ?: listOf()
    set(value) {
        set(DataComponentTypes.LORE, LoreComponent(value))
    }

var ItemStack.displayNameAccordingToNbt: Text
    get() = get(DataComponentTypes.CUSTOM_NAME) ?: get(DataComponentTypes.ITEM_NAME) ?: item.name
    set(value) {
        set(DataComponentTypes.CUSTOM_NAME, value)
    }

fun ItemStack.setCustomName(text: Text) {
    set(DataComponentTypes.CUSTOM_NAME, text)
}
