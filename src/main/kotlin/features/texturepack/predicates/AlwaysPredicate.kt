
package moe.nea.firmament.features.texturepack.predicates

import com.google.gson.JsonElement
import moe.nea.firmament.features.texturepack.FirmamentModelPredicate
import moe.nea.firmament.features.texturepack.FirmamentModelPredicateParser
import net.minecraft.item.ItemStack

object AlwaysPredicate : FirmamentModelPredicate {
    override fun test(stack: ItemStack): Boolean {
        return true
    }

    object Parser : FirmamentModelPredicateParser {
        override fun parse(jsonElement: JsonElement): FirmamentModelPredicate {
            return AlwaysPredicate
        }
    }
}
