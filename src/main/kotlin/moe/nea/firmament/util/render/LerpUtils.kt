/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util.render

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

fun ilerp(a: Float, b: Float, value: Float): Float {
    return (value - a) / (b - a)
}
