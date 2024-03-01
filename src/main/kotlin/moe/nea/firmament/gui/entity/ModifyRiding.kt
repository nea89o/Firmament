/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.entity

import com.google.gson.JsonObject
import net.minecraft.entity.LivingEntity

object ModifyRiding : EntityModifier {
    override fun apply(entity: LivingEntity, info: JsonObject): LivingEntity {
        val newEntity = EntityRenderer.constructEntity(info)
        require(newEntity != null)
        newEntity.startRiding(entity, true)
        return entity
    }

}
