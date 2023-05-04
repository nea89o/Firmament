package moe.nea.notenoughupdates.util

import io.github.moulberry.repo.data.Rarity
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound

@JvmInline
value class SkyblockId(val neuItem: String)

@Serializable
data class HypixelPetInfo(
    val type: String,
    val tier: Rarity,
) {
    val skyblockId get() = SkyblockId("${type.uppercase()};${tier}")
}

private val jsonparser = Json { ignoreUnknownKeys = true }

val ItemStack.extraAttributes: NbtCompound
    get() = getOrCreateSubNbt("ExtraAttributes")

val ItemStack.skyBlockId: SkyblockId?
    get() {
        when (val id = extraAttributes.getString("id")) {
            "PET" -> {
                val jsonString = extraAttributes.getString("petInfo")
                if (jsonString.isNullOrBlank()) return null
                val petInfo =
                    runCatching { jsonparser.decodeFromString<HypixelPetInfo>(jsonString) }
                        .getOrElse { return null }
                return petInfo.skyblockId
            }
            // TODO: RUNE, ENCHANTED_BOOK, PARTY_HAT_CRAB{,_ANIMATED}, ABICASE
            else -> {
                return SkyblockId(id)
            }
        }
    }
