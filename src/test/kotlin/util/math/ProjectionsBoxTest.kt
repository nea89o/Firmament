package moe.nea.firmament.test.util.math

import java.util.stream.Stream
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.streams.asStream
import net.minecraft.util.math.Vec2f
import moe.nea.firmament.util.math.Projections

class ProjectionsBoxTest {
	val Double.degrees get() = Math.toRadians(this)

	@TestFactory
	fun testProjections(): Stream<DynamicTest> {
		return sequenceOf(
			0.0.degrees to Vec2f(1F, 0F),
			63.4349.degrees to Vec2f(0.5F, 1F),
		).map { (angle, expected) ->
			DynamicTest.dynamicTest("ProjectionsBoxTest::projectAngleOntoUnitBox(${angle})") {
				val actual = Projections.Two.projectAngleOntoUnitBox(angle)
				fun msg() = "Expected (${expected.x}, ${expected.y}) got (${actual.x}, ${actual.y})"
				Assertions.assertEquals(expected.x, actual.x, 0.0001F, ::msg)
				Assertions.assertEquals(expected.y, actual.y, 0.0001F, ::msg)
			}
		}.asStream()
	}
}
