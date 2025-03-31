package moe.nea.firmament.util.mc

import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.player.PlayerEntity


val PlayerEntity.mainHandStack get() = this.getEquippedStack(EquipmentSlot.MAINHAND)
