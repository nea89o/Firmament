@file:UseSerializers(DashlessUUIDSerializer::class, InstantAsLongSerializer::class)

package moe.nea.firmament.util.item

import com.mojang.authlib.GameProfile
import com.mojang.authlib.minecraft.MinecraftProfileTexture
import com.mojang.authlib.properties.Property
import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.encodeToString
import net.minecraft.client.texture.PlayerSkinProvider
import moe.nea.firmament.Firmament
import moe.nea.firmament.util.json.DashlessUUIDSerializer
import moe.nea.firmament.util.json.InstantAsLongSerializer

@Serializable
data class MinecraftProfileTextureKt(
    val url: String,
    val metadata: Map<String, String> = mapOf(),
)

@Serializable
data class MinecraftTexturesPayloadKt(
    val textures: Map<MinecraftProfileTexture.Type, MinecraftProfileTextureKt>,
    val profileId: UUID,
    val profileName: String,
    val isPublic: Boolean = true,
    val timestamp: Instant = Clock.System.now(),
)

fun GameProfile.setTextures(textures: MinecraftTexturesPayloadKt) {
    val json = Firmament.json.encodeToString(textures)
    val encoded = java.util.Base64.getEncoder().encodeToString(json.encodeToByteArray())
    properties.put(PlayerSkinProvider.TEXTURES, Property(PlayerSkinProvider.TEXTURES, encoded))
}

