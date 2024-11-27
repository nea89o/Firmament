package moe.nea.firmament.gui.config

import net.minecraft.text.Text

interface EnumRenderer<E : Any> {
	fun getName(option: ManagedOption<E>, value: E): Text

	companion object {
		fun <E : Enum<E>> default() = object : EnumRenderer<E> {
			override fun getName(option: ManagedOption<E>, value: E): Text {
				return Text.translatable(option.rawLabelText + ".choice." + value.name.lowercase())
			}
		}
	}
}
