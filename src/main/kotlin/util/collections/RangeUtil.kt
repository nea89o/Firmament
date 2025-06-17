package moe.nea.firmament.util.collections

import kotlin.math.floor

val ClosedFloatingPointRange<Float>.centre get() = (endInclusive + start) / 2

fun ClosedFloatingPointRange<Float>.nonNegligibleSubSectionsAlignedWith(
	interval: Float
): Iterable<Float> {
	require(interval.isFinite())
	val range = this
	return object : Iterable<Float> {
		override fun iterator(): Iterator<Float> {
			return object : FloatIterator() {
				var polledValue: Float = range.start
				var lastValue: Float = polledValue

				override fun nextFloat(): Float {
					if (!hasNext()) throw NoSuchElementException()
					lastValue = polledValue
					polledValue = Float.NaN
					return lastValue
				}

				override fun hasNext(): Boolean {
					if (!polledValue.isNaN()) {
						return true
					}
					if (lastValue == range.endInclusive)
						return false
					polledValue = (floor(lastValue / interval) + 1) * interval
					if (polledValue > range.endInclusive) {
						polledValue = range.endInclusive
					}
					return true
				}
			}
		}
	}
}
