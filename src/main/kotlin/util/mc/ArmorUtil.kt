package moe.nea.firmament.util.mc

import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity

val LivingEntity.iterableArmorItems
	get() = EquipmentSlot.entries.asSequence()
		.map { it to getEquippedStack(it) }
