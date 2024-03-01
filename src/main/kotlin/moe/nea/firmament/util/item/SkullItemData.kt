/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

@file:UseSerializers(DashlessUUIDSerializer::class, InstantAsLongSerializer::class)

package moe.nea.firmament.util.item

import com.mojang.authlib.GameProfile
import com.mojang.authlib.minecraft.MinecraftProfileTexture
import com.mojang.authlib.properties.Property
import java.util.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.minecraft.block.entity.SkullBlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import moe.nea.firmament.Firmament
import moe.nea.firmament.repo.set
import moe.nea.firmament.util.assertTrueOr
import moe.nea.firmament.util.json.DashlessUUIDSerializer
import moe.nea.firmament.util.json.InstantAsLongSerializer

@Serializable
data class MinecraftProfileTextureKt(
    val url: String,
    val metadata: Map<String, String> = mapOf(),
)

@Serializable
data class MinecraftTexturesPayloadKt(
    val textures: Map<MinecraftProfileTexture.Type, MinecraftProfileTextureKt> = mapOf(),
    val profileId: UUID? = null,
    val profileName: String? = null,
    val isPublic: Boolean = true,
    val timestamp: Instant = Clock.System.now(),
)

fun GameProfile.setTextures(textures: MinecraftTexturesPayloadKt) {
    val json = Firmament.json.encodeToString(textures)
    val encoded = java.util.Base64.getEncoder().encodeToString(json.encodeToByteArray())
    properties.put(propertyTextures, Property(propertyTextures, encoded))
}

private val propertyTextures = "textures"
fun String.padBase64(): String {
    return this + "=".repeat((4 - (this.length % 4)) % 4)
}

fun ItemStack.setEncodedSkullOwner(uuid: UUID, encodedData: String) {
    assert(this.item == Items.PLAYER_HEAD)
    val gameProfile = GameProfile(uuid, "LameGuy123")
    gameProfile.properties.put(propertyTextures, Property(propertyTextures, encodedData.padBase64()))
    val nbt: NbtCompound = this.orCreateNbt
    nbt[SkullBlockEntity.SKULL_OWNER_KEY] = NbtHelper.writeGameProfile(
        NbtCompound(),
        gameProfile
    )
}

val zeroUUID = UUID.fromString("d3cb85e2-3075-48a1-b213-a9bfb62360c1")
fun ItemStack.setSkullOwner(uuid: UUID, url: String) {
    assert(this.item == Items.PLAYER_HEAD)
    val gameProfile = GameProfile(uuid, "LameGuy123")
    gameProfile.setTextures(
        MinecraftTexturesPayloadKt(
            mapOf(MinecraftProfileTexture.Type.SKIN to MinecraftProfileTextureKt(url))
        )
    )
    val nbt: NbtCompound = this.orCreateNbt
    nbt[SkullBlockEntity.SKULL_OWNER_KEY] = NbtHelper.writeGameProfile(
        NbtCompound(),
        gameProfile
    )

}


fun decodeProfileTextureProperty(property: Property): MinecraftTexturesPayloadKt? {
    assertTrueOr(property.name == propertyTextures) { return null }
    return try {
        var encodedF: String = property.value
        while (encodedF.length % 4 != 0 && encodedF.last() == '=') {
            encodedF = encodedF.substring(0, encodedF.length - 1)
        }
        val json = java.util.Base64.getDecoder().decode(encodedF).decodeToString()
        Firmament.json.decodeFromString<MinecraftTexturesPayloadKt>(json)
    } catch (e: Exception) {
        // Malformed profile data
        if (Firmament.DEBUG)
            e.printStackTrace()
        null
    }
}

