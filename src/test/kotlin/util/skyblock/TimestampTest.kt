package moe.nea.firmament.test.util.skyblock

import java.time.Instant
import java.time.ZonedDateTime
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import moe.nea.firmament.test.testutil.ItemResources
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.timestamp

class TimestampTest {

	@Test
	fun testLongTimestamp() {
		Assertions.assertEquals(
			Instant.ofEpochSecond(1658091600),
			ItemResources.loadItem("hyperion").timestamp
		)
	}

	@Test
	fun testStringTimestamp() {
		Assertions.assertEquals(
			ZonedDateTime.of(2021, 10, 11, 15, 39, 0, 0, SBData.hypixelTimeZone).toInstant(),
			ItemResources.loadItem("backpack-in-menu").timestamp
		)
	}
}
