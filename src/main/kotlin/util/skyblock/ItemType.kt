package moe.nea.firmament.util.skyblock

import net.minecraft.item.ItemStack
import moe.nea.firmament.util.directLiteralStringContent
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.petData


@JvmInline
value class ItemType private constructor(val name: String) {
	companion object {
		fun ofName(name: String): ItemType {
			return ItemType(name)
		}

		fun fromItemStack(itemStack: ItemStack): ItemType? {
			if (itemStack.petData != null)
				return PET
			val typeText =
				itemStack.loreAccordingToNbt.lastOrNull()
					?.siblings?.find {
						!it.style.isObfuscated && !it.directLiteralStringContent.isNullOrBlank()
					}?.directLiteralStringContent
			if (typeText != null) {
				val type = typeText.substringAfter(' ', missingDelimiterValue = "").trim()
				if (type.isEmpty()) return null
				return ofName(type)
			}
			return null
		}

		val SWORD = ofName("SWORD")
		val DRILL = ofName("DRILL")
		val PICKAXE = ofName("PICKAXE")
		val GAUNTLET = ofName("GAUNTLET")

		/**
		 * This one is not really official (it never shows up in game).
		 */
		val PET = ofName("PET")
	}

	override fun toString(): String {
		return name
	}
}
