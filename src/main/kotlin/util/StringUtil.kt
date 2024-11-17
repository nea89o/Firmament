package moe.nea.firmament.util

object StringUtil {
	fun String.words(): Sequence<String> {
		return splitToSequence(" ") // TODO: better boundaries
	}

	fun parseIntWithComma(string: String): Int {
		return string.replace(",", "").toInt()
	}

	fun Iterable<String>.unwords() = joinToString(" ")
}
