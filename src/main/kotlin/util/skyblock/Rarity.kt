package moe.nea.firmament.util.skyblock

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import moe.nea.firmament.util.StringUtil.words
import moe.nea.firmament.util.collections.lastNotNullOfOrNull
import moe.nea.firmament.util.mc.DataComponentAccessor
import moe.nea.firmament.util.mc.DataComponentMutator
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
	val text: Component get() = Component.literal(name).setStyle(Style.EMPTY.withColor(colourMap[this]))
	val neuRepoRarity: RepoRarity? = RepoRarity.entries.find { it.name == name }

	companion object {
		// TODO: inline those formattings as fields
		val colourMap = mapOf(
			Rarity.COMMON to ChatFormatting.WHITE,
			Rarity.UNCOMMON to ChatFormatting.GREEN,
			Rarity.RARE to ChatFormatting.BLUE,
			Rarity.EPIC to ChatFormatting.DARK_PURPLE,
			Rarity.LEGENDARY to ChatFormatting.GOLD,
			Rarity.MYTHIC to ChatFormatting.LIGHT_PURPLE,
			Rarity.DIVINE to ChatFormatting.AQUA,
			Rarity.SPECIAL to ChatFormatting.RED,
			Rarity.VERY_SPECIAL to ChatFormatting.RED,
			Rarity.SUPREME to ChatFormatting.DARK_RED,
			Rarity.ULTIMATE to ChatFormatting.DARK_RED,
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

		fun fromItem(itemStack: DataComponentAccessor): Rarity? {
			return fromLore(itemStack.loreAccordingToNbt) ?: fromPetItem(itemStack)
		}

		fun fromPetItem(itemStack: DataComponentAccessor): Rarity? =
			itemStack.petData?.tier?.let(::fromNeuRepo)

		fun fromLore(lore: List<Component>): Rarity? =
			lore.lastNotNullOfOrNull {
				it.unformattedString.words()
					.firstNotNullOfOrNull(::fromString)
			}

	}
}
