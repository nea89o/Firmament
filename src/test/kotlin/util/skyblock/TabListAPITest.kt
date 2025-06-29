package moe.nea.firmament.test.util.skyblock

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import moe.nea.firmament.test.testutil.ItemResources
import moe.nea.firmament.util.skyblock.TabListAPI

class TabListAPITest {
	val tablist = ItemResources.loadTablist("dungeon_hub")

	@Test
	fun checkWithTitle() {
		Assertions.assertEquals(
			listOf(
				"Profile: Strawberry",
				" SB Level: [210] 26/100 XP",
				" Bank: 1.4B",
				" Interest: 12 Hours (689.1k)",
			),
			TabListAPI.getWidgetLines(TabListAPI.WidgetName.PROFILE, includeTitle = true, from = tablist).map { it.string })
	}

	@Test
	fun checkEndOfColumn() {
		Assertions.assertEquals(
			listOf(
				" Bonzo IV: 110/150",
				" Scarf II: 25/50",
				" The Professor IV: 141/150",
				" Thorn I: 29/50",
				" Livid II: 91/100",
				" Sadan V: 388/500",
				" Necron VI: 531/750",
			),
			TabListAPI.getWidgetLines(TabListAPI.WidgetName.COLLECTION, from = tablist).map { it.string }
		)
	}

	@Test
	fun checkWithoutTitle() {
		Assertions.assertEquals(
			listOf(
				" Undead: 1,907",
				" Wither: 318",
			),
			TabListAPI.getWidgetLines(TabListAPI.WidgetName.ESSENCE, from = tablist).map { it.string })
	}
}
