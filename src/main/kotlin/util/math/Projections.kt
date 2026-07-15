package moe.nea.firmament.util.math

import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin
import net.minecraft.world.phys.Vec2
import moe.nea.firmament.util.render.wrapAngle

object Projections {
	object Two {
		val ε = 1e-6
		val π = moe.nea.firmament.util.render.π
		val τ = 2 * π

		fun isNullish(float: Float) = float.absoluteValue < ε

		fun xInterceptOfLine(origin: Vec2, direction: Vec2): Vec2? {
			if (isNullish(direction.x))
				return Vec2(origin.x, 0F)
			if (isNullish(direction.y))
				return null

			val slope = direction.y / direction.x
			return Vec2(origin.x - origin.y / slope, 0F)
		}

		fun interceptAlongCardinal(distanceFromAxis: Float, slope: Float): Float? {
			if (isNullish(slope))
				return null
			return -distanceFromAxis / slope
		}

		@JvmStatic
		fun projectAngleOntoUnitBox(angleRadians: Double): Vec2 {
			val angleRadians = wrapAngle(angleRadians)
			val cx = cos(angleRadians)
			val cy = sin(angleRadians)

			val ex = 1 / cx.absoluteValue
			val ey = 1 / cy.absoluteValue

			val e = minOf(ex, ey)

			return Vec2((cx * e).toFloat(), (cy * e).toFloat())
		}
	}
}
