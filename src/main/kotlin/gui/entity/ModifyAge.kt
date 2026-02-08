
package moe.nea.firmament.gui.entity

import com.google.gson.JsonObject
import net.minecraft.world.entity.AgeableMob
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.entity.monster.zombie.Zombie

object ModifyAge : EntityModifier {
    override fun apply(entity: LivingEntity, info: JsonObject): LivingEntity {
        val isBaby = info["baby"]?.asBoolean ?: false
        if (entity is AgeableMob) {
            entity.age = if (isBaby) -1 else 1
        } else if (entity is Zombie) {
            entity.isBaby = isBaby
        } else if (entity is ArmorStand) {
            entity.isSmall = isBaby
        } else {
            error("Cannot set age for $entity")
        }
        return entity
    }

}
