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
