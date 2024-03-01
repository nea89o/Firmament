/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.entity

import com.google.gson.JsonObject
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.mob.CreeperEntity

object ModifyCharged : EntityModifier {
    override fun apply(entity: LivingEntity, info: JsonObject): LivingEntity {
        require(entity is CreeperEntity)
        entity.dataTracker.set(CreeperEntity.CHARGED, true)
        return entity
    }
}
