package moe.nea.firmament.test.util.skyblock

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import moe.nea.firmament.test.testutil.ItemResources
import moe.nea.firmament.util.mc.RequiresComponents
import moe.nea.firmament.util.skyblock.SBItemUtil.getSearchName

class SBItemUtilTest {
	@OptIn(RequiresComponents::class)
	@TestFactory
	fun searchNameOfEnchantedBooks() =
		listOf(
			"books/feather_falling" to "Feather Falling VI",
			"books/feather_falling_combinable" to "Feather Falling VI",
		).map { (name, searchName) ->
			DynamicTest.dynamicTest("return $searchName for $name") {
				Assertions.assertEquals(searchName, ItemResources.loadItem(name).getSearchName())
			}
		}
}
