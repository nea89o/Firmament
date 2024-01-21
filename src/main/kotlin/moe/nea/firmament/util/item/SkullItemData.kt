/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
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
import net.minecraft.client.texture.PlayerSkinProvider
import moe.nea.firmament.Firmament
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

