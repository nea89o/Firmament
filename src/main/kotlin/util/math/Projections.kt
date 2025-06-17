package moe.nea.firmament.util.math

import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin
import net.minecraft.util.math.Vec2f
import moe.nea.firmament.util.render.wrapAngle

object Projections {
	object Two {
		val ε = 1e-6
		val π = moe.nea.firmament.util.render.π
		val τ = 2 * π

		fun isNullish(float: Float) = float.absoluteValue < ε

		fun xInterceptOfLine(origin: Vec2f, direction: Vec2f): Vec2f? {
			if (isNullish(direction.x))
				return Vec2f(origin.x, 0F)
			if (isNullish(direction.y))
				return null

			val slope = direction.y / direction.x
			return Vec2f(origin.x - origin.y / slope, 0F)
		}

		fun interceptAlongCardinal(distanceFromAxis: Float, slope: Float): Float? {
			if (isNullish(slope))
				return null
			return -distanceFromAxis / slope
		}

		fun projectAngleOntoUnitBox(angleRadians: Double): Vec2f {
			val angleRadians = wrapAngle(angleRadians)
			val cx = cos(angleRadians)
			val cy = sin(angleRadians)

			val ex = 1 / cx.absoluteValue
			val ey = 1 / cy.absoluteValue

			val e = minOf(ex, ey)

			return Vec2f((cx * e).toFloat(), (cy * e).toFloat())
		}
	}
}
