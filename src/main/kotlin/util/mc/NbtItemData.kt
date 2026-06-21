package moe.nea.firmament.util.mc

import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.CommonComponents
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component

val DataComponentAccessor.loreAccordingToNbt: List<Component>
	get() = get(DataComponents.LORE)?.lines ?: listOf()

var DataComponentMutator.loreAccordingToNbt: List<Component>
	get() = (this as DataComponentAccessor).loreAccordingToNbt
	set(value) {
		set(DataComponents.LORE, ItemLore(value))
	}

val DataComponentAccessor.displayNameAccordingToNbt: Component
	get() = get(DataComponents.CUSTOM_NAME) ?: get(DataComponents.ITEM_NAME) ?: CommonComponents.EMPTY

var DataComponentMutator.displayNameAccordingToNbt: Component
	get() = (this as DataComponentAccessor).displayNameAccordingToNbt
	set(value) {
		set(DataComponents.CUSTOM_NAME, value)
	}

fun DataComponentMutator.setCustomName(text: Component) {
	set(DataComponents.CUSTOM_NAME, text)
}
