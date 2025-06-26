@file:OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)

package moe.nea.firmament.util

import java.util.regex.Matcher
import java.util.regex.Pattern
import org.intellij.lang.annotations.Language
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

inline fun <T> String.ifMatches(regex: Regex, block: (MatchResult) -> T): T? =
	regex.matchEntire(this)?.let(block)

inline fun <T> Pattern.useMatch(string: String?, block: Matcher.() -> T): T? {
	contract {
		callsInPlace(block, InvocationKind.AT_MOST_ONCE)
	}
	return string
		?.let(this::matcher)
		?.takeIf(Matcher::matches)
		?.let(block)
}

fun <T> String.ifDropLast(suffix: String, block: (String) -> T): T? {
	if (endsWith(suffix)) {
		return block(dropLast(suffix.length))
	}
	return null
}

@Language("RegExp")
val TIME_PATTERN = "[0-9]+[ms]"

@Language("RegExp")
val SHORT_NUMBER_FORMAT = "[0-9]+(?:,[0-9]+)*(?:\\.[0-9]+)?[kKmMbB]?"


val siScalars = mapOf(
	'k' to 1_000.0,
	'K' to 1_000.0,
	'm' to 1_000_000.0,
	'M' to 1_000_000.0,
	'b' to 1_000_000_000.0,
	'B' to 1_000_000_000.0,
)

fun parseTimePattern(text: String): Duration {
	val length = text.dropLast(1).toInt()
	return when (text.last()) {
		'm' -> length.minutes
		's' -> length.seconds
		else -> error("Invalid pattern for time $text")
	}
}

fun parseShortNumber(string: String): Double {
	if (string.startsWith("-")) return -parseShortNumber(string.substring(1))
	if (string.startsWith("+")) return parseShortNumber(string.substring(1))
	var k = string.replace(",", "")
	val scalar = k.last()
	var scalarMultiplier = siScalars[scalar]
	if (scalarMultiplier == null) {
		scalarMultiplier = 1.0
	} else {
		k = k.dropLast(1)
	}
	return k.toDouble() * scalarMultiplier
}
