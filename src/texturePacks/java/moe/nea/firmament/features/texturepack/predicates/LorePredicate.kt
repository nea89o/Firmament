
package moe.nea.firmament.features.texturepack.predicates

import com.google.gson.JsonElement
import moe.nea.firmament.features.texturepack.FirmamentModelPredicate
import moe.nea.firmament.features.texturepack.FirmamentModelPredicateParser
import moe.nea.firmament.features.texturepack.StringMatcher
import net.minecraft.world.item.ItemStack
import moe.nea.firmament.util.mc.accessor
import moe.nea.firmament.util.mc.loreAccordingToNbt

class LorePredicate(val matcher: StringMatcher) : FirmamentModelPredicate {
    object Parser : FirmamentModelPredicateParser {
        override fun parse(jsonElement: JsonElement): FirmamentModelPredicate {
            return LorePredicate(StringMatcher.parse(jsonElement))
        }
    }

    override fun test(stack: ItemStack): Boolean {
        val lore = stack.accessor().loreAccordingToNbt
        return lore.any { matcher.matches(it) }
    }
}
