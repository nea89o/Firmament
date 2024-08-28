
package moe.nea.firmament.gui.entity

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlin.experimental.and
import kotlin.experimental.or
import net.minecraft.client.util.SkinTextures
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerModelPart
import net.minecraft.util.Identifier

object ModifyPlayerSkin : EntityModifier {
    val playerModelPartIndex = PlayerModelPart.entries.associateBy { it.getName() }
    override fun apply(entity: LivingEntity, info: JsonObject): LivingEntity {
        require(entity is GuiPlayer)
        info["cape"]?.let {
            entity.capeTexture = Identifier.of(it.asString)
        }
        info["skin"]?.let {
            entity.skinTexture = Identifier.of(it.asString)
        }
        info["slim"]?.let {
            entity.model = if (it.asBoolean) SkinTextures.Model.SLIM else SkinTextures.Model.WIDE
        }
        info["parts"]?.let {
            var trackedData = entity.dataTracker.get(PlayerEntity.PLAYER_MODEL_PARTS)
            if (it is JsonPrimitive && it.isBoolean) {
                trackedData = (if (it.asBoolean) -1 else 0).toByte()
            } else {
                val obj = it.asJsonObject
                for ((k, v) in obj.entrySet()) {
                    val part = playerModelPartIndex[k]!!
                    trackedData = if (v.asBoolean) {
                        trackedData and (part.bitFlag.inv().toByte())
                    } else {
                        trackedData or (part.bitFlag.toByte())
                    }
                }
            }
            entity.dataTracker.set(PlayerEntity.PLAYER_MODEL_PARTS, trackedData)
        }
        return entity
    }

}
