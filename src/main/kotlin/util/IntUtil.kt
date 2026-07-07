package moe.nea.firmament.util

object IntUtil {
	data class RGBA(val r: Int, val g: Int, val b: Int, val a: Int)

	fun Int.toRGBA(): RGBA {
		return RGBA(
			r = (this shr 16) and 0xFF, g = (this shr 8) and 0xFF, b = this and 0xFF, a = (this shr 24) and 0xFF
		)
	}

	fun String.romanToInt(): Int {
		val romanValues = mapOf(
			'I' to 1, 'V' to 5, 'X' to 10, 'L' to 50,
			'C' to 100, 'D' to 500, 'M' to 1000
		)

		var result = 0
		var prev = 0

		for (char in reversed()) {
			val curr = romanValues[char] ?: return 0
			if (curr < prev) result -= curr else result += curr
			prev = curr
		}
		return result
	}

	// Source: https://stackoverflow.com/questions/19266018/converting-integer-to-roman-numeral converted to Kotlin by Claude
	fun Int.toRomanNumeral(): String {
		if (this !in 1..3999) return this.toString()

		val romanData = listOf(
			1000 to "M", 900 to "CM",
			500 to "D", 400 to "CD",
			100 to "C", 90 to "XC",
			50 to "L", 40 to "XL",
			10 to "X", 9 to "IX",
			5 to "V", 4 to "IV",
			1 to "I"
		)

		var value = this
		val result = StringBuilder()

		for ((num, numeral) in romanData) {
			while (value >= num) {
				result.append(numeral)
				value -= num
			}
		}

		return result.toString()
	}

}
