/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.texturepack

import com.mojang.authlib.minecraft.MinecraftProfileTexture
import com.mojang.authlib.properties.Property
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import net.minecraft.block.SkullBlock
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.component.type.ProfileComponent
import net.minecraft.util.Identifier
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.CustomItemModelEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.IdentityCharacteristics
import moe.nea.firmament.util.item.decodeProfileTextureProperty
import moe.nea.firmament.util.skyBlockId

object CustomSkyBlockTextures : FirmamentFeature {
    override val identifier: String
        get() = "custom-skyblock-textures"

    object TConfig : ManagedConfig(identifier) {
        val enabled by toggle("enabled") { true }
        val skullsEnabled by toggle("skulls-enabled") { true }
        val cacheDuration by integer("cache-duration", 0, 20) { 1 }
        val enableModelOverrides by toggle("model-overrides") { true }
    }

    override val config: ManagedConfig
        get() = TConfig

    @Subscribe
    fun onTick(it: TickEvent) {
        if (TConfig.cacheDuration < 1 || it.tickCount % TConfig.cacheDuration == 0) {
            CustomItemModelEvent.clearCache()
            skullTextureCache.clear()
        }
    }

    @Subscribe
    fun onCustomModelId(it: CustomItemModelEvent) {
        if (!TConfig.enabled) return
        val id = it.itemStack.skyBlockId ?: return
        it.overrideModel = ModelIdentifier("firmskyblock", id.identifier.path, "inventory")
    }

    private val skullTextureCache = mutableMapOf<IdentityCharacteristics<ProfileComponent>, Any>()
    private val sentinelPresentInvalid = Object()

    private val mcUrlRegex = "https?://textures.minecraft.net/texture/([a-fA-F0-9]+)".toRegex()

    fun getSkullId(textureProperty: Property): String? {
        val texture = decodeProfileTextureProperty(textureProperty) ?: return null
        val textureUrl =
            texture.textures[MinecraftProfileTexture.Type.SKIN]?.url ?: return null
        val mcUrlData = mcUrlRegex.matchEntire(textureUrl) ?: return null
        return mcUrlData.groupValues[1]
    }

    fun getSkullTexture(profile: ProfileComponent): Identifier? {
        val id = getSkullId(profile.properties["textures"].firstOrNull() ?: return null) ?: return null
        return Identifier("firmskyblock", "textures/placedskull/$id.png")
    }

    fun modifySkullTexture(
        type: SkullBlock.SkullType?,
        component: ProfileComponent?,
        cir: CallbackInfoReturnable<RenderLayer>
    ) {
        if (type != SkullBlock.Type.PLAYER) return
        if (!TConfig.skullsEnabled) return
        if (component == null) return
        val ic = IdentityCharacteristics(component)

        val n = skullTextureCache.getOrPut(ic) {
            val id = getSkullTexture(component) ?: return@getOrPut sentinelPresentInvalid
            if (!MinecraftClient.getInstance().resourceManager.getResource(id).isPresent) {
                return@getOrPut sentinelPresentInvalid
            }
            return@getOrPut id
        }
        if (n === sentinelPresentInvalid) return
        cir.returnValue = RenderLayer.getEntityTranslucent(n as Identifier)
    }
}
