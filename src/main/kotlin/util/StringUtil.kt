package moe.nea.firmament.util

object StringUtil {
	fun String.words(): Sequence<String> {
		return splitToSequence(" ") // TODO: better boundaries
	}

	fun Iterable<String>.unwords() = joinToString(" ")
}
