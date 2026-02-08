package moe.nea.firmament.gui.entity

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlin.experimental.and
import kotlin.experimental.or
import net.minecraft.client.entity.ClientAvatarEntity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Avatar
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.player.PlayerModelPart
import net.minecraft.world.entity.player.PlayerModelType
import net.minecraft.world.entity.player.PlayerSkin
import net.minecraft.core.ClientAsset
import net.minecraft.resources.Identifier

object ModifyPlayerSkin : EntityModifier {
	val playerModelPartIndex = PlayerModelPart.entries.associateBy { it.id }
	override fun apply(entity: LivingEntity, info: JsonObject): LivingEntity {
		require(entity is GuiPlayer)
		var capeTexture = entity.skinTextures.cape
		var model = entity.skinTextures.model
		var bodyTexture = entity.skinTextures.body
		fun mkTexAsset(id: Identifier) = ClientAsset.ResourceTexture(id, id)
		info["cape"]?.let {
			capeTexture = mkTexAsset(Identifier.parse(it.asString))
		}
		info["skin"]?.let {
			bodyTexture = mkTexAsset(Identifier.parse(it.asString))
		}
		info["slim"]?.let {
			model = if (it.asBoolean) PlayerModelType.SLIM else PlayerModelType.WIDE
		}
		info["parts"]?.let {
			var trackedData = entity.entityData.get(Avatar.DATA_PLAYER_MODE_CUSTOMISATION)
			if (it is JsonPrimitive && it.isBoolean) {
				trackedData = (if (it.asBoolean) -1 else 0).toByte()
			} else {
				val obj = it.asJsonObject
				for ((k, v) in obj.entrySet()) {
					val part = playerModelPartIndex[k]!!
					trackedData = if (v.asBoolean) {
						trackedData and (part.mask.inv().toByte())
					} else {
						trackedData or (part.mask.toByte())
					}
				}
			}
			entity.entityData.set(Player.DATA_PLAYER_MODE_CUSTOMISATION, trackedData)
		}
		entity.skinTextures = PlayerSkin(
			bodyTexture, capeTexture, null, model, true
		)
		return entity
	}

}
