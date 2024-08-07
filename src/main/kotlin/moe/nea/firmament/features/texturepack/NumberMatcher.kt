
package moe.nea.firmament.features.texturepack

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import moe.nea.firmament.util.useMatch

abstract class NumberMatcher {
    abstract fun test(number: Number): Boolean


    companion object {
        fun parse(jsonElement: JsonElement): NumberMatcher? {
            if (jsonElement is JsonPrimitive) {
                if (jsonElement.isString) {
                    val string = jsonElement.asString
                    return parseRange(string) ?: parseOperator(string)
                }
                if (jsonElement.isNumber) {
                    val number = jsonElement.asNumber
                    val hasDecimals = (number.toString().contains("."))
                    return MatchNumberExact(if (hasDecimals) number.toLong() else number.toDouble())
                }
            }
            return null
        }

        private val intervalSpec =
            "(?<beginningOpen>[\\[\\(])(?<beginning>[0-9.]+)?,(?<ending>[0-9.]+)?(?<endingOpen>[\\]\\)])"
                .toPattern()

        fun parseRange(string: String): RangeMatcher? {
            intervalSpec.useMatch<Nothing>(string) {
                // Open in the set-theory sense, meaning does not include its end.
                val beginningOpen = group("beginningOpen") == "("
                val endingOpen = group("endingOpen") == ")"
                val beginning = group("beginning")?.toDouble()
                val ending = group("ending")?.toDouble()
                return RangeMatcher(beginning, !beginningOpen, ending, !endingOpen)
            }
            return null
        }

        enum class Operator(val operator: String) {
            LESS("<") {
                override fun matches(comparisonResult: Int): Boolean {
                    return comparisonResult < 0
                }
            },
            LESS_EQUALS("<=") {
                override fun matches(comparisonResult: Int): Boolean {
                    return comparisonResult <= 0
                }
            },
            GREATER(">") {
                override fun matches(comparisonResult: Int): Boolean {
                    return comparisonResult > 0
                }
            },
            GREATER_EQUALS(">=") {
                override fun matches(comparisonResult: Int): Boolean {
                    return comparisonResult >= 0
                }
            },
            ;

            abstract fun matches(comparisonResult: Int): Boolean
        }

        private val operatorPattern = "(?<operator>${Operator.entries.joinToString("|") {it.operator}})(?<value>[0-9.]+)".toPattern()

        fun parseOperator(string: String): OperatorMatcher? {
            operatorPattern.useMatch<Nothing>(string) {
                val operatorName = group("operator")
                val operator = Operator.entries.find { it.operator == operatorName }!!
                val value = group("value").toDouble()
                return OperatorMatcher(operator, value)
            }
            return null
        }

        data class OperatorMatcher(val operator: Operator, val value: Double) : NumberMatcher() {
            override fun test(number: Number): Boolean {
                return operator.matches(number.toDouble().compareTo(value))
            }
        }


        data class MatchNumberExact(val number: Number) : NumberMatcher() {
            override fun test(number: Number): Boolean {
                return when (this.number) {
                    is Double -> number.toDouble() == this.number.toDouble()
                    else -> number.toLong() == this.number.toLong()
                }
            }
        }

        data class RangeMatcher(
            val beginning: Double?,
            val beginningInclusive: Boolean,
            val ending: Double?,
            val endingInclusive: Boolean,
        ) : NumberMatcher() {
            override fun test(number: Number): Boolean {
                val value = number.toDouble()
                if (beginning != null) {
                    if (beginningInclusive) {
                        if (value < beginning) return false
                    } else {
                        if (value <= beginning) return false
                    }
                }
                if (ending != null) {
                    if (endingInclusive) {
                        if (value > ending) return false
                    } else {
                        if (value >= ending) return false
                    }
                }
                return true
            }
        }
    }

}
