package moe.nea.firmament.util.skyblock

import net.minecraft.world.item.ItemStack
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.petData


data class ItemType private constructor(val name: String) {
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
			for (loreLine in itemStack.loreAccordingToNbt) {
				val words = loreLine.string.split(" ").filter { it.length > 1 } // removes [recomb?]
				if (words.any { word -> word.any { it.isLowerCase() } }) continue // only uppercase
				val rarityIdx = words.indexOfFirst { Rarity.fromString(it) != null } // skips [SHINY?] [VERY?]
				if (rarityIdx == -1) continue // no rarity in line
				val typeWords = words.subList(rarityIdx + 1, words.size)
				if (typeWords.isEmpty()) continue
				return ofName(typeWords.joinToString(" "))
			}
			return null
		}


		// TODO: some of those are not actual in game item types, but rather ones included in the repository to splat to multiple in game types. codify those somehow

		val SWORD = ofName("SWORD")
		val DRILL = ofName("DRILL")
		val PICKAXE = ofName("PICKAXE")
		val AXE = ofName("AXE")
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
		val SHOVEL = ofName("SHOVEL")
		val BOW = ofName("BOW")
		val HOE = ofName("HOE")
		val CARNIVAL_MASK = ofName("CARNIVAL MASK")

		val NIL = ofName("__NIL")

		/**
		 * This one is not really official (it never shows up in game).
		 */
		val PET = ofName("PET")
	}

	val dungeonVariant get() = ofName("DUNGEON $name")

	val isDungeon get() = name.startsWith("DUNGEON ")

	val nonDungeonVariant get() = ofName(name.removePrefix("DUNGEON "))

	override fun toString(): String {
		return name
	}
}
