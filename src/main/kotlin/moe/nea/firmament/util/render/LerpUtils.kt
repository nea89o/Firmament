/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util.render

import me.shedaniel.math.Color

val pi = Math.PI
val tau = Math.PI * 2
fun lerpAngle(a: Float, b: Float, progress: Float): Float {
    // TODO: there is at least 10 mods to many in here lol
    val shortestAngle = ((((b.mod(tau) - a.mod(tau)).mod(tau)) + tau + pi).mod(tau)) - pi
    return ((a + (shortestAngle) * progress).mod(tau)).toFloat()
}

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

