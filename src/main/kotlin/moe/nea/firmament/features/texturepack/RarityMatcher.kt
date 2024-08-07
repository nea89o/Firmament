
package moe.nea.firmament.features.texturepack

import com.google.gson.JsonElement
import io.github.moulberry.repo.data.Rarity
import moe.nea.firmament.util.useMatch

abstract class RarityMatcher {
    abstract fun match(rarity: Rarity): Boolean

    companion object {
        fun parse(jsonElement: JsonElement): RarityMatcher {
            val string = jsonElement.asString
            val range = parseRange(string)
            if (range != null) return range
            return Exact(Rarity.valueOf(string))
        }

        private val allRarities = Rarity.entries.joinToString("|", "(?:", ")")
        private val intervalSpec =
            "(?<beginningOpen>[\\[\\(])(?<beginning>$allRarities)?,(?<ending>$allRarities)?(?<endingOpen>[\\]\\)])"
                .toPattern()

        fun parseRange(string: String): RangeMatcher? {
            intervalSpec.useMatch<Nothing>(string) {
                // Open in the set-theory sense, meaning does not include its end.
                val beginningOpen = group("beginningOpen") == "("
                val endingOpen = group("endingOpen") == ")"
                val beginning = group("beginning")?.let(Rarity::valueOf)
                val ending = group("ending")?.let(Rarity::valueOf)
                return RangeMatcher(beginning, !beginningOpen, ending, !endingOpen)
            }
            return null
        }

    }

    data class Exact(val expected: Rarity) : RarityMatcher() {
        override fun match(rarity: Rarity): Boolean {
            return rarity == expected
        }
    }

    data class RangeMatcher(
        val beginning: Rarity?,
        val beginningInclusive: Boolean,
        val ending: Rarity?,
        val endingInclusive: Boolean,
    ) : RarityMatcher() {
        override fun match(rarity: Rarity): Boolean {
            if (beginning != null) {
                if (beginningInclusive) {
                    if (rarity < beginning) return false
                } else {
                    if (rarity <= beginning) return false
                }
            }
            if (ending != null) {
                if (endingInclusive) {
                    if (rarity > ending) return false
                } else {
                    if (rarity >= ending) return false
                }
            }
            return true
        }
    }

}
