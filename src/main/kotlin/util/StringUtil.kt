package moe.nea.firmament.util

object StringUtil {
	fun String.words(): Sequence<String> {
		return splitToSequence(" ") // TODO: better boundaries
	}

	fun String.camelWords(): Sequence<String> {
		return splitToSequence(camelWordStart)
	}

	private val camelWordStart = Regex("((?<=[a-z])(?=[A-Z]))| ")

	fun parseIntWithComma(string: String): Int {
		return string.replace(",", "").toInt()
	}

	fun String.title() = replaceFirstChar { it.titlecase() }

	fun Iterable<String>.unwords() = joinToString(" ")
	fun nextLexicographicStringOfSameLength(string: String): String {
		val next = StringBuilder(string)
		while (next.lastOrNull() == Character.MAX_VALUE) next.setLength(next.length - 1)
		if (next.isEmpty()) return "" // There is no upper bound. Fall back to the empty string
		val lastIdx = next.indices.last
		next[lastIdx] = (next[lastIdx] + 1)
		return next.toString()
	}

}
