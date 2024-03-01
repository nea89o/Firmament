/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.entity

import com.google.gson.JsonNull
import com.google.gson.JsonObject
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.passive.AbstractHorseEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import moe.nea.firmament.gui.entity.EntityRenderer.fakeWorld

object ModifyHorse : EntityModifier {
    override fun apply(entity: LivingEntity, info: JsonObject): LivingEntity {
        require(entity is AbstractHorseEntity)
        var entity: AbstractHorseEntity = entity
        info["kind"]?.let {
            entity = when (it.asString) {
                "skeleton" -> EntityType.SKELETON_HORSE.create(fakeWorld)!!
                "zombie" -> EntityType.ZOMBIE_HORSE.create(fakeWorld)!!
                "mule" -> EntityType.MULE.create(fakeWorld)!!
                "donkey" -> EntityType.DONKEY.create(fakeWorld)!!
                "horse" -> EntityType.HORSE.create(fakeWorld)!!
                else -> error("Unknown horse kind $it")
            }
        }
        info["armor"]?.let {
            if (it is JsonNull) {
                entity.setHorseArmor(ItemStack.EMPTY)
            } else {
                when (it.asString) {
                    "iron" -> entity.setHorseArmor(ItemStack(Items.IRON_HORSE_ARMOR))
                    "golden" -> entity.setHorseArmor(ItemStack(Items.GOLDEN_HORSE_ARMOR))
                    "diamond" -> entity.setHorseArmor(ItemStack(Items.DIAMOND_HORSE_ARMOR))
                    else -> error("Unknown horse armor $it")
                }
            }
        }
        info["saddled"]?.let {
            entity.setIsSaddled(it.asBoolean)
        }
        return entity
    }

}

fun AbstractHorseEntity.setIsSaddled(shouldBeSaddled: Boolean) {
    val oldFlag = dataTracker.get(AbstractHorseEntity.HORSE_FLAGS)
    dataTracker.set(
        AbstractHorseEntity.HORSE_FLAGS,
        if (shouldBeSaddled) oldFlag or AbstractHorseEntity.SADDLED_FLAG.toByte()
        else oldFlag and AbstractHorseEntity.SADDLED_FLAG.toByte().inv()
    )
}

fun AbstractHorseEntity.setHorseArmor(itemStack: ItemStack) {
    items.setStack(1, itemStack)
}
