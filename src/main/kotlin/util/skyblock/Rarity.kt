package moe.nea.firmament.util.skyblock

import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import moe.nea.firmament.util.StringUtil.words
import moe.nea.firmament.util.collections.lastNotNullOfOrNull
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.petData
import moe.nea.firmament.util.unformattedString

typealias RepoRarity = io.github.moulberry.repo.data.Rarity

enum class Rarity(vararg altNames: String) {
	COMMON,
	UNCOMMON,
	RARE,
	EPIC,
	LEGENDARY("LEGENJERRY"),
	MYTHIC,
	DIVINE,
	SUPREME,
	SPECIAL,
	VERY_SPECIAL,
	UNKNOWN
	;

	val names = setOf(name) + altNames

	val neuRepoRarity: RepoRarity? = RepoRarity.entries.find { it.name == name }

	companion object {
		val byName = entries.flatMap { en -> en.names.map { it to en } }.toMap()
		val fromNeuRepo = entries.associateBy { it.neuRepoRarity }

		fun fromNeuRepo(repo: RepoRarity): Rarity? {
			return fromNeuRepo[repo]
		}

		fun fromString(name: String): Rarity? {
			return byName[name]
		}

		fun fromTier(tier: Int): Rarity? {
			return entries.getOrNull(tier)
		}

		fun fromItem(itemStack: ItemStack): Rarity? {
			return fromLore(itemStack.loreAccordingToNbt) ?: fromPetItem(itemStack)
		}

		fun fromPetItem(itemStack: ItemStack): Rarity? =
			itemStack.petData?.tier?.let(::fromNeuRepo)

		fun fromLore(lore: List<Text>): Rarity? =
			lore.lastNotNullOfOrNull {
				it.unformattedString.words()
					.firstNotNullOfOrNull(::fromString)
			}

	}
}
