package moe.nea.firmament.util.render

import me.shedaniel.math.Color
import kotlin.math.absoluteValue

val π = Math.PI
val τ = Math.PI * 2
fun lerpAngle(a: Float, b: Float, progress: Float): Float {
	// TODO: there is at least 10 mods to many in here lol
	if (((b - a).absoluteValue - π).absoluteValue < 0.0001) {
		return lerp(a, b, progress)
	}
	val shortestAngle = ((((b.mod(τ) - a.mod(τ)).mod(τ)) + τ + π).mod(τ)) - π
	return ((a + (shortestAngle) * progress).mod(τ)).toFloat()
}

fun wrapAngle(angle: Float): Float = (angle.mod(τ) + τ).mod(τ).toFloat()
fun wrapAngle(angle: Double): Double = (angle.mod(τ) + τ).mod(τ)

fun lerp(a: Float, b: Float, progress: Float): Float {
	return a + (b - a) * progress
}

fun lerp(a: Int, b: Int, progress: Float): Int {
	return (a + (b - a) * progress).toInt()
}

fun ilerp(a: Float, b: Float, value: Float): Float {
	return (value - a) / (b - a)
}

fun lerp(a: Color, b: Color, progress: Float): Color {
	return Color.ofRGBA(
		lerp(a.red, b.red, progress),
		lerp(a.green, b.green, progress),
		lerp(a.blue, b.blue, progress),
		lerp(a.alpha, b.alpha, progress),
	)
}

