package moe.nea.firmament.util

object IntUtil {
	data class RGBA(val r: Int, val g: Int, val b: Int, val a: Int)

	fun Int.toRGBA(): RGBA {
		return RGBA(
			r = (this shr 16) and 0xFF, g = (this shr 8) and 0xFF, b = this and 0xFF, a = (this shr 24) and 0xFF
		)
	}

}
