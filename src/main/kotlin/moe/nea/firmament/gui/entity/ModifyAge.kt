/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.entity

import com.google.gson.JsonObject
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.entity.passive.PassiveEntity

object ModifyAge : EntityModifier {
    override fun apply(entity: LivingEntity, info: JsonObject): LivingEntity {
        val isBaby = info["baby"]?.asBoolean ?: false
        if (entity is PassiveEntity) {
            entity.breedingAge = if (isBaby) -1 else 1
        } else if (entity is ZombieEntity) {
            entity.isBaby = isBaby
        } else if (entity is ArmorStandEntity) {
            entity.isSmall = isBaby
        } else {
            error("Cannot set age for $entity")
        }
        return entity
    }

}
