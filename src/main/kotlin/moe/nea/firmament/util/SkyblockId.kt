/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package moe.nea.firmament.util

import io.github.moulberry.repo.data.NEUItem
import io.github.moulberry.repo.data.Rarity
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier

/**
 * A skyblock item id, as used by the NEU repo.
 * This is not exactly the format used by HyPixel, but is mostly the same.
 * Usually this id splits an id used by HyPixel into more sub items. For example `PET` becomes `$PET_ID;$PET_RARITY`,
 * with those values extracted from other metadata.
 */
@JvmInline
@Serializable
value class SkyblockId(val neuItem: String) {
    val identifier get() = Identifier("skyblockitem", neuItem.lowercase().replace(";", "__"))

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
        private val bazaarEnchantmentRegex = "ENCHANTMENT_(\\D*)_(\\d+)".toRegex()
        val NULL: SkyblockId = SkyblockId("null")
    }
}

val NEUItem.skyblockId get() = SkyblockId(skyblockItemId)

@Serializable
data class HypixelPetInfo(
    val type: String,
    val tier: Rarity,
) {
    val skyblockId get() = SkyblockId("${type.uppercase()};${tier.ordinal}")
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
