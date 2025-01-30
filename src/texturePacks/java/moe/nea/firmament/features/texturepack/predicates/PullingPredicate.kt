package moe.nea.firmament.features.texturepack.predicates

import com.google.gson.JsonElement
import net.minecraft.entity.LivingEntity
import net.minecraft.item.BowItem
import net.minecraft.item.ItemStack
import moe.nea.firmament.features.texturepack.FirmamentModelPredicate
import moe.nea.firmament.features.texturepack.FirmamentModelPredicateParser

class PullingPredicate(val percentage: Double) : FirmamentModelPredicate {
	companion object {
		val AnyPulling = PullingPredicate(0.1)
	}

	object Parser : FirmamentModelPredicateParser {
		override fun parse(jsonElement: JsonElement): FirmamentModelPredicate? {
			return PullingPredicate(jsonElement.asDouble)
		}
	}

	override fun test(stack: ItemStack, holder: LivingEntity?): Boolean {
		if (holder == null) return false
		return BowItem.getPullProgress(holder.itemUseTime) >= percentage
	}

}
