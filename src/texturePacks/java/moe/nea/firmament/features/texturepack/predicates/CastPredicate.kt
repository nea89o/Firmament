package moe.nea.firmament.features.texturepack.predicates

import com.google.gson.JsonElement
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import moe.nea.firmament.features.texturepack.FirmamentModelPredicate
import moe.nea.firmament.features.texturepack.FirmamentModelPredicateParser

class CastPredicate : FirmamentModelPredicate {
	object Parser : FirmamentModelPredicateParser {
		override fun parse(jsonElement: JsonElement): FirmamentModelPredicate? {
			if (jsonElement.asDouble >= 1) return CastPredicate()
			return NotPredicate(arrayOf(CastPredicate()))
		}
	}

	override fun test(stack: ItemStack, holder: LivingEntity?): Boolean {
		return (holder as? PlayerEntity)?.fishHook != null && holder.mainHandStack === stack
	}

	override fun test(stack: ItemStack): Boolean {
		return false
	}
}
