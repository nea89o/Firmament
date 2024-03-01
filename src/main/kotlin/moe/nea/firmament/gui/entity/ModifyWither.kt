/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.entity

import com.google.gson.JsonObject
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.boss.WitherEntity

object ModifyWither : EntityModifier {
    override fun apply(entity: LivingEntity, info: JsonObject): LivingEntity {
        require(entity is WitherEntity)
        info["tiny"]?.let {
            entity.setInvulTimer(if (it.asBoolean) 800 else 0)
        }
        info["armored"]?.let {
            entity.health = if (it.asBoolean) 1F else entity.maxHealth
        }
        return entity
    }

}
