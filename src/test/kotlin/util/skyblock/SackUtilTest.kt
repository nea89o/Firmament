package moe.nea.firmament.test.util.skyblock

import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Assertions
import moe.nea.firmament.test.testutil.ItemResources
import moe.nea.firmament.util.skyblock.SackUtil
import moe.nea.firmament.util.skyblock.SkyBlockItems

class SackUtilTest : AnnotationSpec() {
	@Test
	fun testOneRottenFlesh() {
		Assertions.assertEquals(
			listOf(
				SackUtil.SackUpdate(SkyBlockItems.ROTTEN_FLESH, "Rotten Flesh", 1)
			),
			SackUtil.getUpdatesFromMessage(ItemResources.loadText("sacks/gain-rotten-flesh"))
		)
	}

	@Test
	fun testAFewRegularItems() {
		Assertions.assertEquals(
			listOf(
				SackUtil.SackUpdate(SkyBlockItems.ROTTEN_FLESH, "Rotten Flesh", 1)
			),
			SackUtil.getUpdatesFromMessage(ItemResources.loadText("sacks/gain-and-lose-regular"))
		)
	}
}
