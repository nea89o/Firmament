@file:UseSerializers(DashlessUUIDSerializer::class)

package moe.nea.firmament.util

import com.mojang.serialization.Codec
import io.github.moulberry.repo.data.NEUIngredient
import io.github.moulberry.repo.data.NEUItem
import io.github.moulberry.repo.data.Rarity
import java.util.Optional
import java.util.UUID
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
value class SkyblockId(val neuItem: String) {
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
) {
	val skyblockId get() = SkyblockId("${type.uppercase()};${tier.ordinal}")
}

private val jsonparser = Json { ignoreUnknownKeys = true }

val ItemStack.extraAttributes: NbtCompound
	get() {
		val customData = get(DataComponentTypes.CUSTOM_DATA) ?: run {
			val component = NbtComponent.of(NbtCompound())
			set(DataComponentTypes.CUSTOM_DATA, component)
			component
		}
		return customData.nbt
	}

val ItemStack.skyblockUUIDString: String?
	get() = extraAttributes.getString("uuid")?.takeIf { it.isNotBlank() }

val ItemStack.skyblockUUID: UUID?
	get() = skyblockUUIDString?.let { UUID.fromString(it) }

private val petDataCache = WeakCache.memoize<ItemStack, Optional<HypixelPetInfo>>("PetInfo") {
	val jsonString = it.extraAttributes.getString("petInfo")
	if (jsonString.isNullOrBlank()) return@memoize Optional.empty()
	runCatching { jsonparser.decodeFromString<HypixelPetInfo>(jsonString) }
		.getOrElse { null }.intoOptional()
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
		return when (val id = extraAttributes.getString("id")) {
			"" -> {
				null
			}

			"PET" -> {
				petData?.skyblockId ?: SkyblockId.PET_NULL
			}

			"RUNE", "UNIQUE_RUNE" -> {
				val runeData = extraAttributes.getCompound("runes")
				val runeKind = runeData.keys.singleOrNull()
				if (runeKind == null) SkyblockId("RUNE")
				else SkyblockId("${runeKind.uppercase()}_RUNE;${runeData.getInt(runeKind)}")
			}

			"ABICASE" -> {
				SkyblockId("ABICASE_${extraAttributes.getString("model").uppercase()}")
			}

			"ENCHANTED_BOOK" -> {
				val enchantmentData = extraAttributes.getCompound("enchantments")
				val enchantName = enchantmentData.keys.singleOrNull()
				if (enchantName == null) SkyblockId("ENCHANTED_BOOK")
				else SkyblockId("${enchantName.uppercase()};${enchantmentData.getInt(enchantName)}")
			}

			// TODO: PARTY_HAT_CRAB{,_ANIMATED,_SLOTH},POTION
			else -> {
				SkyblockId(id)
			}
		}
	}

