package moe.nea.firmament.test.util.math

import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import moe.nea.firmament.util.math.GChainReconciliation
import moe.nea.firmament.util.math.GChainReconciliation.rotated

class GChainReconciliationTest : AnnotationSpec() {

	fun <T> assertEqualCycles(
		expected: List<T>,
		actual: List<T>
	) {
		for (offset in expected.indices) {
			val rotated = expected.rotated(offset)
			val matchesAtRotation = run {
				for ((i, v) in actual.withIndex()) {
					if (rotated[i % rotated.size] != v)
						return@run false
				}
				true
			}
			if (matchesAtRotation)
				return
		}
		assertEquals(expected, actual, "Expected arrays to be cycle equivalent")
	}

	@Test
	fun testUnfixableCycleNotBeingModified() {
		assertEquals(
			listOf(1, 2, 3, 4, 6, 1, 2, 3, 4, 6),
			GChainReconciliation.reconcileCycles(
				listOf(1, 2, 3, 4, 6, 1, 2, 3, 4, 6),
				listOf(2, 3, 4, 5, 1, 2, 3, 4, 5, 1)
			)
		)
	}

	@Test
	fun testMultipleIndependentHoles() {
		assertEqualCycles(
			listOf(1, 2, 3, 4, 5, 6),
			GChainReconciliation.reconcileCycles(
				listOf(1, 3, 4, 5, 6, 1, 3, 4, 5, 6),
				listOf(2, 3, 4, 5, 1, 2, 3, 4, 5, 1)
			)
		)

	}

	@Test
	fun testBigHole() {
		assertEqualCycles(
			listOf(1, 2, 3, 4, 5, 6),
			GChainReconciliation.reconcileCycles(
				listOf(1, 4, 5, 6, 1, 4, 5, 6),
				listOf(2, 3, 4, 5, 1, 2, 3, 4, 5, 1)
			)
		)

	}

	@Test
	fun testOneMissingBeingDetected() {
		assertEqualCycles(
			listOf(1, 2, 3, 4, 5, 6),
			GChainReconciliation.reconcileCycles(
				listOf(1, 2, 3, 4, 5, 6, 1, 2, 3, 4, 5, 6),
				listOf(2, 3, 4, 5, 1, 2, 3, 4, 5, 1)
			)
		)
	}
}
