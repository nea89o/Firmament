/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.entity

import com.google.gson.JsonObject
import net.minecraft.client.util.SkinTextures
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Identifier

object ModifyPlayerSkin : EntityModifier {
    override fun apply(entity: LivingEntity, info: JsonObject): LivingEntity {
        require(entity is GuiPlayer)
        info["cape"]?.let {
            entity.capeTexture = Identifier(it.asString)
        }
        info["skin"]?.let {
            entity.skinTexture = Identifier(it.asString)
        }
        info["slim"]?.let {
            entity.model = if (it.asBoolean) SkinTextures.Model.SLIM else SkinTextures.Model.WIDE
        }
        info["parts"]?.let {
            // TODO: support parts
        }
        return entity
    }

}
