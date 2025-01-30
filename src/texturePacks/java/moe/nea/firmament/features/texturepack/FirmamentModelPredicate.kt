package moe.nea.firmament.features.texturepack

import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

interface FirmamentModelPredicate {
	fun test(stack: ItemStack, holder: LivingEntity?): Boolean = test(stack)
	fun test(stack: ItemStack): Boolean = test(stack, null)
}
