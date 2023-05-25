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

@JvmInline
value class SkyblockId(val neuItem: String) {
    val identifier get() = Identifier("skyblockitem", neuItem.lowercase().replace(";", "__"))

    companion object {
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
