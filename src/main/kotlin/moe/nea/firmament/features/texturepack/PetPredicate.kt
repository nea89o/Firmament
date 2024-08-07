
package moe.nea.firmament.features.texturepack

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.minecraft.item.ItemStack
import moe.nea.firmament.repo.ExpLadders
import moe.nea.firmament.util.petData

class PetPredicate(
    val petId: StringMatcher?,
    val tier: RarityMatcher?,
    val exp: NumberMatcher?,
    val candyUsed: NumberMatcher?,
    val level: NumberMatcher?,
) : FirmamentModelPredicate {

    override fun test(stack: ItemStack): Boolean {
        val petData = stack.petData ?: return false
        if (petId != null) {
            if (!petId.matches(petData.type)) return false
        }
        if (exp != null) {
            if (!exp.test(petData.exp)) return false
        }
        if (candyUsed != null) {
            if (!candyUsed.test(petData.candyUsed)) return false
        }
        if (tier != null) {
            if (!tier.match(petData.tier)) return false
        }
        val levelData by lazy(LazyThreadSafetyMode.NONE) {
            ExpLadders.getExpLadder(petData.type, petData.tier)
                .getPetLevel(petData.exp)
        }
        if (level != null) {
            if (!level.test(levelData.currentLevel)) return false
        }
        return true
    }

    object Parser : FirmamentModelPredicateParser {
        override fun parse(jsonElement: JsonElement): FirmamentModelPredicate? {
            if (jsonElement.isJsonPrimitive) {
                return PetPredicate(StringMatcher.Equals(jsonElement.asString, false), null, null, null, null)
            }
            if (jsonElement !is JsonObject) return null
            val idMatcher = jsonElement["id"]?.let(StringMatcher::parse)
            val expMatcher = jsonElement["exp"]?.let(NumberMatcher::parse)
            val levelMatcher = jsonElement["level"]?.let(NumberMatcher::parse)
            val candyMatcher = jsonElement["candyUsed"]?.let(NumberMatcher::parse)
            val tierMatcher = jsonElement["tier"]?.let(RarityMatcher::parse)
            return PetPredicate(
                idMatcher,
                tierMatcher,
                expMatcher,
                candyMatcher,
                levelMatcher,
            )
        }
    }

    override fun toString(): String {
        return super.toString()
    }
}
