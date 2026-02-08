package moe.nea.firmament.gui.entity

import com.google.gson.JsonNull
import com.google.gson.JsonObject
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.equine.AbstractHorse
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import moe.nea.firmament.gui.entity.EntityRenderer.fakeWorld

object ModifyHorse : EntityModifier {
	override fun apply(entity: LivingEntity, info: JsonObject): LivingEntity {
		require(entity is AbstractHorse)
		var entity: AbstractHorse = entity
		info["kind"]?.let {
			entity = when (it.asString) {
				"skeleton" -> EntityType.SKELETON_HORSE.create(fakeWorld, EntitySpawnReason.LOAD)!!
				"zombie" -> EntityType.ZOMBIE_HORSE.create(fakeWorld, EntitySpawnReason.LOAD)!!
				"mule" -> EntityType.MULE.create(fakeWorld, EntitySpawnReason.LOAD)!!
				"donkey" -> EntityType.DONKEY.create(fakeWorld, EntitySpawnReason.LOAD)!!
				"horse" -> EntityType.HORSE.create(fakeWorld, EntitySpawnReason.LOAD)!!
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

fun AbstractHorse.setIsSaddled(shouldBeSaddled: Boolean) {
	this.setItemSlot(
		EquipmentSlot.SADDLE,
		if (shouldBeSaddled) ItemStack(Items.SADDLE)
		else ItemStack.EMPTY
	)
}

fun AbstractHorse.setHorseArmor(itemStack: ItemStack) {
	bodyArmorItem = itemStack
}
