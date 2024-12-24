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

		private val obfuscatedRegex = "§[kK].*?(§[0-9a-fA-FrR]|$)".toRegex()
		fun fromEscapeCodeLore(lore: String): ItemType? {
			return lore.replace(obfuscatedRegex, "").trim().substringAfter(" ", "")
				.takeIf { it.isNotEmpty() }
				?.let(::ofName)
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
			return itemStack.loreAccordingToNbt.lastOrNull()?.directLiteralStringContent?.let(::fromEscapeCodeLore)
		}

		// TODO: some of those are not actual in game item types, but rather ones included in the repository to splat to multiple in game types. codify those somehow

		val SWORD = ofName("SWORD")
		val DRILL = ofName("DRILL")
		val PICKAXE = ofName("PICKAXE")
		val GAUNTLET = ofName("GAUNTLET")
		val LONGSWORD = ofName("LONG SWORD")
		val EQUIPMENT = ofName("EQUIPMENT")
		val FISHING_WEAPON = ofName("FISHING WEAPON")
		val CLOAK = ofName("CLOAK")
		val BELT = ofName("BELT")
		val NECKLACE = ofName("NECKLACE")
		val BRACELET = ofName("BRACELET")
		val GLOVES = ofName("GLOVES")
		val ROD = ofName("ROD")
		val FISHING_ROD = ofName("FISHING ROD")
		val VACUUM = ofName("VACUUM")
		val CHESTPLATE = ofName("CHESTPLATE")
		val LEGGINGS = ofName("LEGGINGS")
		val HELMET = ofName("HELMET")
		val BOOTS = ofName("BOOTS")
		val NIL = ofName("__NIL")

		/**
		 * This one is not really official (it never shows up in game).
		 */
		val PET = ofName("PET")
	}

	override fun toString(): String {
		return name
	}
}
