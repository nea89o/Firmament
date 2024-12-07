package moe.nea.firmament.features.texturepack.predicates

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import moe.nea.firmament.features.texturepack.CustomModelOverrideParser
import moe.nea.firmament.features.texturepack.FirmamentModelPredicate
import moe.nea.firmament.features.texturepack.FirmamentModelPredicateParser
import net.minecraft.item.ItemStack

class AndPredicate(val children: Array<FirmamentModelPredicate>) : FirmamentModelPredicate {
    override fun test(stack: ItemStack): Boolean {
        return children.all { it.test(stack) }
    }

    object Parser : FirmamentModelPredicateParser {
        override fun parse(jsonElement: JsonElement): FirmamentModelPredicate {
            val children =
                (jsonElement as JsonArray)
                    .flatMap {
	                    CustomModelOverrideParser.parsePredicates(it as JsonObject)
                    }
                    .toTypedArray()
            return AndPredicate(children)
        }

    }
}
