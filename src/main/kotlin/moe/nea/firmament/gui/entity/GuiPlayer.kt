/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.entity

import com.mojang.authlib.GameProfile
import java.util.*
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.util.DefaultSkinHelper
import net.minecraft.client.util.SkinTextures
import net.minecraft.client.util.SkinTextures.Model
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * @see moe.nea.firmament.init.EarlyRiser
 */
fun makeGuiPlayer(world: FakeWorld): GuiPlayer {
    val constructor = GuiPlayer::class.java.getDeclaredConstructor(
        World::class.java,
        BlockPos::class.java,
        Float::class.javaPrimitiveType,
        GameProfile::class.java
    )
    return constructor.newInstance(world, BlockPos.ORIGIN, 0F, GameProfile(UUID.randomUUID(), "Linnea"))
}

class GuiPlayer(world: ClientWorld?, profile: GameProfile?) : AbstractClientPlayerEntity(world, profile) {
    override fun isSpectator(): Boolean {
        return false
    }

    override fun isCreative(): Boolean {
        return false
    }

    var skinTexture: Identifier = DefaultSkinHelper.getSkinTextures(this.getUuid()).texture
    var capeTexture: Identifier? = null
    var model: Model = Model.WIDE
    override fun getSkinTextures(): SkinTextures {
        return SkinTextures(
            skinTexture,
            null,
            capeTexture,
            null,
            model,
            true
        )
    }
}
