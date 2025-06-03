@file:UseSerializers(DashlessUUIDSerializer::class)

package moe.nea.firmament.util

import com.mojang.serialization.Codec
import io.github.moulberry.repo.data.NEUIngredient
import io.github.moulberry.repo.data.NEUItem
import io.github.moulberry.repo.data.Rarity
import java.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.Json
import kotlin.jvm.optionals.getOrNull
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.util.Identifier
import moe.nea.firmament.repo.ExpLadders
import moe.nea.firmament.repo.ItemCache.asItemStack
import moe.nea.firmament.repo.set
import moe.nea.firmament.util.collections.WeakCache
import moe.nea.firmament.util.json.DashlessUUIDSerializer

/**
 * A SkyBlock item id, as used by the NEU repo.
 * This is not exactly the format used by HyPixel, but is mostly the same.
 * Usually this id splits an id used by HyPixel into more sub items. For example `PET` becomes `$PET_ID;$PET_RARITY`,
 * with those values extracted from other metadata.
 */
@JvmInline
@Serializable
value class SkyblockId(val neuItem: String) : Comparable<SkyblockId> {
	val identifier
		get() = Identifier.of("skyblockitem",
		                      neuItem.lowercase().replace(";", "__")
			                      .replace(":", "___")
			                      .replace(illlegalPathRegex) {
				                      it.value.toCharArray()
					                      .joinToString("") { "__" + it.code.toString(16).padStart(4, '0') }
			                      })

	override fun toString(): String {
		return neuItem
	}

	override fun compareTo(other: SkyblockId): Int {
		return neuItem.compareTo(other.neuItem)
	}

	/**
	 * A bazaar stock item id, as returned by the HyPixel bazaar api endpoint.
	 * These are not equivalent to the in-game ids, or the NEU repo ids, and in fact, do not refer to items, but instead
	 * to bazaar stocks. The main difference from [SkyblockId]s is concerning enchanted books. There are probably more,
	 * but for now this holds.
	 */
	@JvmInline
	@Serializable
	value class BazaarStock(val bazaarId: String) {
		fun toRepoId(): SkyblockId {
			bazaarEnchantmentRegex.matchEntire(bazaarId)?.let {
				return SkyblockId("${it.groupValues[1]};${it.groupValues[2]}")
			}
			return SkyblockId(bazaarId.replace(":", "-"))
		}
	}

	companion object {
		val COINS: SkyblockId = SkyblockId(NEUIngredient.NEU_SENTINEL_COINS)
		val SENTINEL_EMPTY: SkyblockId = SkyblockId(NEUIngredient.NEU_SENTINEL_EMPTY)
		private val bazaarEnchantmentRegex = "ENCHANTMENT_(\\D*)_(\\d+)".toRegex()
		val NULL: SkyblockId = SkyblockId("null")
		val PET_NULL: SkyblockId = SkyblockId("null_pet")
		private val illlegalPathRegex = "[^a-z0-9_.-/]".toRegex()
		val CODEC = Codec.STRING.xmap({ SkyblockId(it) }, { it.neuItem })
		val PACKET_CODEC: PacketCodec<in RegistryByteBuf, SkyblockId> =
			PacketCodecs.STRING.xmap({ SkyblockId(it) }, { it.neuItem })
	}
}

val NEUItem.skyblockId get() = SkyblockId(skyblockItemId)
val NEUIngredient.skyblockId get() = SkyblockId(itemId)

fun NEUItem.guessRecipeId(): String? {
	if (!skyblockItemId.contains(";")) return skyblockItemId
	val item = this.asItemStack()
	val (id, extraId) = skyblockItemId.split(";")
	if (item.item == Items.ENCHANTED_BOOK) {
		return "ENCHANTED_BOOK_${id}_${extraId}"
	}
	if (item.petData != null) return id
	return null
}

@Serializable
data class HypixelPetInfo(
	val type: String,
	val tier: Rarity,
	val exp: Double = 0.0,
	val candyUsed: Int = 0,
	val uuid: UUID? = null,
	val active: Boolean = false,
	val heldItem: String? = null,
) {
	val skyblockId get() = SkyblockId("${type.uppercase()};${tier.ordinal}") // TODO: is this ordinal set up correctly?
	val level get() = ExpLadders.getExpLadder(type, tier).getPetLevel(exp)
}

private val jsonparser = Json { ignoreUnknownKeys = true }

var ItemStack.extraAttributes: NbtCompound
	set(value) {
		set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(value))
	}
	get() {
		val customData = get(DataComponentTypes.CUSTOM_DATA) ?: run {
			val component = NbtComponent.of(NbtCompound())
			set(DataComponentTypes.CUSTOM_DATA, component)
			component
		}
		return customData.nbt
	}

fun ItemStack.modifyExtraAttributes(block: (NbtCompound) -> Unit) {
	val baseNbt = get(DataComponentTypes.CUSTOM_DATA)?.copyNbt() ?: NbtCompound()
	block(baseNbt)
	set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(baseNbt))
}

val ItemStack.skyblockUUIDString: String?
	get() = extraAttributes.getString("uuid").getOrNull()?.takeIf { it.isNotBlank() }

val ItemStack.skyblockUUID: UUID?
	get() = skyblockUUIDString?.let { UUID.fromString(it) }

private val petDataCache = WeakCache.memoize<ItemStack, Optional<HypixelPetInfo>>("PetInfo") {
	val jsonString = it.extraAttributes.getString("petInfo")
		.getOrNull()
	if (jsonString.isNullOrBlank()) return@memoize Optional.empty()
	ErrorUtil.catch<HypixelPetInfo?>("Could not decode hypixel pet info") {
		jsonparser.decodeFromString<HypixelPetInfo>(jsonString)
	}
		.or { null }.intoOptional()
}

fun ItemStack.getUpgradeStars(): Int {
	return extraAttributes.getInt("upgrade_level").getOrNull()?.takeIf { it > 0 }
		?: extraAttributes.getInt("dungeon_item_level").getOrNull()?.takeIf { it > 0 }
		?: 0
}

@Serializable
@JvmInline
value class ReforgeId(val id: String)

fun ItemStack.getReforgeId(): ReforgeId? {
	return extraAttributes.getString("modifier").getOrNull()?.takeIf { it.isNotBlank() }?.let(::ReforgeId)
}

val ItemStack.petData: HypixelPetInfo?
	get() = petDataCache(this).getOrNull()

fun ItemStack.setSkyBlockFirmamentUiId(uiId: String) = setSkyBlockId(SkyblockId("FIRMAMENT_UI_$uiId"))
fun ItemStack.setSkyBlockId(skyblockId: SkyblockId): ItemStack {
	this.extraAttributes["id"] = skyblockId.neuItem
	return this
}

val ItemStack.skyBlockId: SkyblockId?
	get() {
		return when (val id = extraAttributes.getString("id").getOrNull()) {
			"", null -> {
				null
			}

			"PET" -> {
				petData?.skyblockId ?: SkyblockId.PET_NULL
			}

			"RUNE", "UNIQUE_RUNE" -> {
				val runeData = extraAttributes.getCompound("runes")
					.getOrNull()
				val runeKind = runeData?.keys?.singleOrNull()
				if (runeKind == null) SkyblockId("RUNE")
				else SkyblockId("${runeKind.uppercase()}_RUNE;${runeData.getInt(runeKind).getOrNull()}")
			}

			"ABICASE" -> {
				SkyblockId("ABICASE_${extraAttributes.getString("model").getOrNull()?.uppercase()}")
			}

			"ENCHANTED_BOOK" -> {
				val enchantmentData = extraAttributes.getCompound("enchantments")
					.getOrNull()
				val enchantName = enchantmentData?.keys?.singleOrNull()
				if (enchantName == null) SkyblockId("ENCHANTED_BOOK")
				else SkyblockId("${enchantName.uppercase()};${enchantmentData.getInt(enchantName).getOrNull()}")
			}

			"ATTRIBUTE_SHARD" -> {
				val attributeData = extraAttributes.getCompound("attributes").getOrNull()
				val attributeName = attributeData?.keys?.singleOrNull()
				if (attributeName == null) SkyblockId("ATTRIBUTE_SHARD")
				else SkyblockId(
					"ATTRIBUTE_SHARD_${attributeName.uppercase()};${
						attributeData.getInt(attributeName).getOrNull()
					}"
				)
			}

			"POTION" -> {
				val potionData = extraAttributes.getString("potion").getOrNull()
				val potionName = extraAttributes.getString("potion_name").getOrNull()
				val potionLevel = extraAttributes.getInt("potion_level").getOrNull()
				val potionType = extraAttributes.getString("potion_type").getOrNull()
				when {
					potionName != null -> SkyblockId("POTION_${potionName.uppercase()};$potionLevel")
					potionData != null -> SkyblockId("POTION_${potionData.uppercase()};$potionLevel")
					potionType != null -> SkyblockId("POTION${potionType.uppercase()}")
					else -> SkyblockId("WATER_BOTTLE")
				}
			}

			// TODO: PARTY_HAT_CRAB{,_ANIMATED,_SLOTH}
			else -> {
				SkyblockId(id)
			}
		}
	}

