/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

@file:UseSerializers(DashlessUUIDSerializer::class)

package moe.nea.firmament.util

import io.github.moulberry.repo.data.NEUItem
import io.github.moulberry.repo.data.Rarity
import java.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import moe.nea.firmament.util.json.DashlessUUIDSerializer

/**
 * A skyblock item id, as used by the NEU repo.
 * This is not exactly the format used by HyPixel, but is mostly the same.
 * Usually this id splits an id used by HyPixel into more sub items. For example `PET` becomes `$PET_ID;$PET_RARITY`,
 * with those values extracted from other metadata.
 */
@JvmInline
@Serializable
value class SkyblockId(val neuItem: String) {
    val identifier get() = Identifier("skyblockitem", neuItem.lowercase().replace(";", "__").replace(":", "___"))
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
        val COINS: SkyblockId = SkyblockId("SKYBLOCK_COIN")
        private val bazaarEnchantmentRegex = "ENCHANTMENT_(\\D*)_(\\d+)".toRegex()
        val NULL: SkyblockId = SkyblockId("null")
        val PET_NULL: SkyblockId = SkyblockId("null_pet")
    }
}

val NEUItem.skyblockId get() = SkyblockId(skyblockItemId)

@Serializable
data class HypixelPetInfo(
    val type: String,
    val tier: Rarity,
    val exp: Double = 0.0,
    val candyUsed: Int = 0,
    val uuid: UUID? = null,
) {
    val skyblockId get() = SkyblockId("${type.uppercase()};${tier.ordinal}")
}

private val jsonparser = Json { ignoreUnknownKeys = true }

val ItemStack.extraAttributes: NbtCompound
    get() = getOrCreateSubNbt("ExtraAttributes")

val ItemStack.skyblockUUIDString: String?
    get() = extraAttributes.getString("uuid")?.takeIf { it.isNotBlank() }

val ItemStack.skyblockUUID: UUID?
    get() = skyblockUUIDString?.let { UUID.fromString(it) }

val ItemStack.petData: HypixelPetInfo?
    get() {
        val jsonString = extraAttributes.getString("petInfo")
        if (jsonString.isNullOrBlank()) return null
        return runCatching { jsonparser.decodeFromString<HypixelPetInfo>(jsonString) }
            .getOrElse { return null }
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

            // TODO: PARTY_HAT_CRAB{,_ANIMATED,_SLOTH}
            else -> {
                SkyblockId(id)
            }
        }
    }

