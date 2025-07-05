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
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.petData
import moe.nea.firmament.util.unformattedString

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
	ULTIMATE,
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
			Rarity.ULTIMATE to Formatting.DARK_RED,
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

		fun fromLore(lore: List<Text>): Rarity? =
			lore.lastNotNullOfOrNull {
				it.unformattedString.words()
					.firstNotNullOfOrNull(::fromString)
			}

	}
}
