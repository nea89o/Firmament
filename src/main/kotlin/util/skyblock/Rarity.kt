package moe.nea.firmament.util.skyblock

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.item.ItemStack
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import moe.nea.firmament.util.StringUtil.words
import moe.nea.firmament.util.collections.lastNotNullOfOrNull
import moe.nea.firmament.util.directLiteralStringContent
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.petData
import moe.nea.firmament.util.prepend
import moe.nea.firmament.util.prependHypixelified
import moe.nea.firmament.util.removeColorCodes
import moe.nea.firmament.util.unformattedString
import moe.nea.firmament.util.withColor

typealias RepoRarity = io.github.moulberry.repo.data.Rarity

@Serializable(with = Rarity.Serializer::class)
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

	object Serializer : KSerializer<Rarity> {
		override val descriptor: SerialDescriptor
			get() = PrimitiveSerialDescriptor(Rarity::class.java.name, PrimitiveKind.STRING)

		override fun deserialize(decoder: Decoder): Rarity {
			return valueOf(decoder.decodeString().replace(" ", "_"))
		}

		override fun serialize(encoder: Encoder, value: Rarity) {
			encoder.encodeString(value.name)
		}
	}

	val names = setOf(name) + altNames
	val text: Text get() = Text.literal(name).setStyle(Style.EMPTY.withColor(colourMap[this]))
	val neuRepoRarity: RepoRarity? = RepoRarity.entries.find { it.name == name }

	fun recombobulate(): Rarity = Rarity.entries.getOrElse(ordinal + 1) { this }

	fun colour() = colourMap[this] ?: Formatting.WHITE

	companion object {
		// TODO: inline those formattings as fields
		val colourMap = mapOf(
			Rarity.COMMON to Formatting.WHITE,
			Rarity.UNCOMMON to Formatting.GREEN,
			Rarity.RARE to Formatting.BLUE,
			Rarity.EPIC to Formatting.DARK_PURPLE,
			Rarity.LEGENDARY to Formatting.GOLD,
			Rarity.MYTHIC to Formatting.LIGHT_PURPLE,
			Rarity.DIVINE to Formatting.AQUA,
			Rarity.SPECIAL to Formatting.RED,
			Rarity.VERY_SPECIAL to Formatting.RED,
			Rarity.SUPREME to Formatting.DARK_RED,
		)
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

		fun fromStringLore(lore: List<String>): Rarity? {
			return lore.lastNotNullOfOrNull {
				it.removeColorCodes().words().firstNotNullOfOrNull(::fromString)
			}
		}

		fun findLoreIndex(lore: List<Text>): Int {
			return lore.indexOfLast {
				it.unformattedString.words().any { fromString(it) != null }
			}
		}

		fun fromLore(lore: List<Text>): Rarity? =
			lore.lastNotNullOfOrNull {
				it.unformattedString.words()
					.firstNotNullOfOrNull(::fromString)
			}

		fun recombobulateLore(lore: List<Text>): List<Text> {
			val before = fromLore(lore) ?: return lore
			val rarityIndex = findLoreIndex(lore)
			if (rarityIndex < 0) return lore
			val after = before.recombobulate()
			val col = after.colour()
			val loreMut = lore.toMutableList()
			val obfuscatedTag = Text.literal("a")
				.withColor(col)
				.styled { it.withObfuscated(true) }
			val rarityLine = loreMut[rarityIndex].copy()
				.prependHypixelified(Text.literal(" "))
				.prepend(obfuscatedTag)
				.append(Text.literal(" "))
				.append(obfuscatedTag)
			(rarityLine.siblings as MutableList<Text>)
				.replaceAll {
					var content = it.directLiteralStringContent
					before.names.forEach {
						content = content?.replace(it, after.name)
					}
					val editedText = (if (content != it.directLiteralStringContent)
						Text.literal(content) else it.copy())
					editedText.withColor(col)
				}
			loreMut[rarityIndex] = rarityLine
			return loreMut
		}

	}
}
