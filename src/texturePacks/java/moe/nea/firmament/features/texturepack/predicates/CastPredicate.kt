package moe.nea.firmament.features.texturepack.predicates

import com.google.gson.JsonElement
import net.minecraft.item.ItemStack
import moe.nea.firmament.features.texturepack.FirmamentModelPredicate
import moe.nea.firmament.features.texturepack.FirmamentModelPredicateParser
import moe.nea.firmament.util.MC

class CastPredicate : FirmamentModelPredicate {
	object Parser : FirmamentModelPredicateParser {
		override fun parse(jsonElement: JsonElement): FirmamentModelPredicate? {
			if (jsonElement.asDouble >= 1) return CastPredicate()
			return NotPredicate(arrayOf(CastPredicate()))
		}
	}

	override fun test(stack: ItemStack): Boolean {
		return MC.player?.fishHook != null // TODO pass through more of the model predicate context
	}
}
