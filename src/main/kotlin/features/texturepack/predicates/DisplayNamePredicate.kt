
package moe.nea.firmament.features.texturepack.predicates

import com.google.gson.JsonElement
import moe.nea.firmament.features.texturepack.FirmamentModelPredicate
import moe.nea.firmament.features.texturepack.FirmamentModelPredicateParser
import moe.nea.firmament.features.texturepack.StringMatcher
import net.minecraft.item.ItemStack
import moe.nea.firmament.util.mc.displayNameAccordingToNbt

data class DisplayNamePredicate(val stringMatcher: StringMatcher) : FirmamentModelPredicate {
    override fun test(stack: ItemStack): Boolean {
        val display = stack.displayNameAccordingToNbt
        return stringMatcher.matches(display)
    }

    object Parser : FirmamentModelPredicateParser {
        override fun parse(jsonElement: JsonElement): FirmamentModelPredicate {
            return DisplayNamePredicate(StringMatcher.parse(jsonElement))
        }
    }
}
