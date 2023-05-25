/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import kotlin.math.atan

private fun calculateAngleFromOffsets(xOffset: Double, zOffset: Double): Double {
    var angleX = Math.toDegrees(Math.acos(xOffset / 0.04f))
    var angleZ = Math.toDegrees(Math.asin(zOffset / 0.04f))
    if (xOffset < 0) {
        angleZ = 180 - angleZ
    }
    if (zOffset < 0) {
        angleX = 360 - angleX
    }
    angleX %= 360.0
    angleZ %= 360.0
    if (angleX < 0) angleX += 360.0
    if (angleZ < 0) angleZ += 360.0
    var dist = angleX - angleZ
    if (dist < -180) dist += 360.0
    if (dist > 180) dist -= 360.0
    return angleZ + dist / 2
}

fun main() {
    for(i in 0..10) {
        for (j in 0..10) {
            println("${calculateAngleFromOffsets(i.toDouble(),j.toDouble())} ${atan(i.toDouble() / j.toDouble())}")
        }
    }
}
