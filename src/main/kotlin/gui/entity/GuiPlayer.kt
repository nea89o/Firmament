package moe.nea.firmament.gui.entity

import net.minecraft.client.entity.ClientMannequin
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.player.PlayerSkin
import net.minecraft.world.level.Level
import moe.nea.firmament.util.MC

fun makeGuiPlayer(world: Level): GuiPlayer {
	val player = GuiPlayer(MC.instance.level!!)
	return player
}

class GuiPlayer(world: ClientLevel) : ClientMannequin(world, MC.instance.playerSkinRenderCache()) {
	override fun isSpectator(): Boolean {
		return false
	}

	override fun shouldShowName(): Boolean {
		return false
	}

	var skinTextures: PlayerSkin = DefaultPlayerSkin.get(this.uuid) // TODO: 1.21.10
	override fun getSkin(): PlayerSkin {
		return skinTextures
	}
}
