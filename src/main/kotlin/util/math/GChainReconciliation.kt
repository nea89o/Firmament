package moe.nea.firmament.util.math

import kotlin.math.min

/**
 * Algorithm for (sort of) cheap reconciliation of two cycles with missing frames.
 */
object GChainReconciliation {
	// Step one: Find the most common element and shift the arrays until it is at the start in both (this could be just rotating until minimal levenshtein distance or smth. that would be way better for cycles with duplicates, but i do not want to implement levenshtein as well)
	// Step two: Find the first different element.
	// Step three: Find the next index of both of the elements.
	// Step four: Insert the element that is further away.

	fun <T> Iterable<T>.frequencies(): Map<T, Int> {
		val acc = mutableMapOf<T, Int>()
		for (t in this) {
			acc.compute(t, { _, old -> (old ?: 0) + 1 })
		}
		return acc
	}

	fun <T> findMostCommonlySharedElement(
		leftChain: List<T>,
		rightChain: List<T>,
	): T {
		val lf = leftChain.frequencies()
		val rf = rightChain.frequencies()
		val mostCommonlySharedElement = lf.maxByOrNull { min(it.value, rf[it.key] ?: 0) }?.key
		if (mostCommonlySharedElement == null || mostCommonlySharedElement !in rf)
			error("Could not find a shared element")
		return mostCommonlySharedElement
	}

	fun <T> List<T>.getMod(index: Int): T {
		return this[index.mod(size)]
	}

	fun <T> List<T>.rotated(offset: Int): List<T> {
		val newList = mutableListOf<T>()
		for (index in indices) {
			newList.add(getMod(index - offset))
		}
		return newList
	}

	fun <T> shiftToFront(list: List<T>, element: T): List<T> {
		val shiftDistance = list.indexOf(element)
		require(shiftDistance >= 0)
		return list.rotated(-shiftDistance)
	}

	fun <T> List<T>.indexOfOrMaxInt(element: T): Int = indexOf(element).takeUnless { it < 0 } ?: Int.MAX_VALUE

	fun <T> reconcileCycles(
		leftChain: List<T>,
		rightChain: List<T>,
	): List<T> {
		val mostCommonElement = findMostCommonlySharedElement(leftChain, rightChain)
		val left = shiftToFront(leftChain, mostCommonElement).toMutableList()
		val right = shiftToFront(rightChain, mostCommonElement).toMutableList()

		var index = 0
		while (index < left.size && index < right.size) {
			val leftEl = left[index]
			val rightEl = right[index]
			if (leftEl == rightEl) {
				index++
				continue
			}
			val nextLeftInRight = right.subList(index, right.size)
				.indexOfOrMaxInt(leftEl)

			val nextRightInLeft = left.subList(index, left.size)
				.indexOfOrMaxInt(rightEl)
			if (nextLeftInRight < nextRightInLeft) {
				left.add(index, rightEl)
			} else if (nextRightInLeft < nextLeftInRight) {
				right.add(index, leftEl)
			} else {
				index++
			}
		}
		return if (left.size < right.size) right else left
	}

	fun <T> isValidCycle(longList: List<T>, cycle: List<T>): Boolean {
		for ((i, value) in longList.withIndex()) {
			if (cycle.getMod(i) != value)
				return false
		}
		return true
	}

	fun <T> List<T>.shortenCycle(): List<T> {
		for (i in (1..<size)) {
			if (isValidCycle(this, subList(0, i)))
				return subList(0, i)
		}
		return this
	}

}
