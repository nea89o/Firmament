package moe.nea.firmament.features.texturepack

import kotlinx.serialization.Serializable
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

@Serializable(with = FirmamentRootPredicateSerializer::class)
interface FirmamentModelPredicate {
	fun test(stack: ItemStack, holder: LivingEntity?): Boolean = test(stack)
	fun test(stack: ItemStack): Boolean = test(stack, null)
}
