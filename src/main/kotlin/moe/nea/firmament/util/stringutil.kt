
package moe.nea.firmament.util

fun parseIntWithComma(string: String): Int {
    return string.replace(",", "").toInt()
}
