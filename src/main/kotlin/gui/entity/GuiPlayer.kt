package moe.nea.firmament.gui.entity

import com.mojang.authlib.GameProfile
import java.util.UUID
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.util.DefaultSkinHelper
import net.minecraft.client.util.SkinTextures
import net.minecraft.client.util.SkinTextures.Model
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

/**
 * @see moe.nea.firmament.init.EarlyRiser
 */
fun makeGuiPlayer(world: World): GuiPlayer {
	val constructor = GuiPlayer::class.java.getDeclaredConstructor(
		World::class.java,
		BlockPos::class.java,
		Float::class.javaPrimitiveType,
		GameProfile::class.java
	)
	val player = constructor.newInstance(world, BlockPos.ORIGIN, 0F, GameProfile(UUID.randomUUID(), "Linnea"))
	player.postInit()
	return player
}

class GuiPlayer(world: ClientWorld?, profile: GameProfile?) : AbstractClientPlayerEntity(world, profile) {
	override fun isSpectator(): Boolean {
		return false
	}

	fun postInit() {
		skinTexture = DefaultSkinHelper.getSkinTextures(this.getUuid()).texture
		lastVelocity = Vec3d.ZERO
		model = Model.WIDE
	}

	override fun isCreative(): Boolean {
		return false
	}

	override fun shouldRenderName(): Boolean {
		return false
	}

	lateinit var skinTexture: Identifier
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
