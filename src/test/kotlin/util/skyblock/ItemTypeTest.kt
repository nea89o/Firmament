package moe.nea.firmament.test.util.skyblock

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import moe.nea.firmament.test.testutil.ItemResources
import moe.nea.firmament.util.skyblock.ItemType

class ItemTypeTest {
	@TestFactory
	fun fromItemstack() =
		listOf(
			"pets/lion-item" to ItemType.PET,
			"pets/rabbit-selected" to ItemType.PET,
			"pets/mithril-golem-not-selected" to ItemType.PET,
			"aspect-of-the-void" to ItemType.SWORD,
			"titanium-drill" to ItemType.DRILL,
			"diamond-pickaxe" to ItemType.PICKAXE,
			"gemstone-gauntlet" to ItemType.GAUNTLET,
		).map { (name, typ) ->
			DynamicTest.dynamicTest("return $typ for $name") {
				Assertions.assertEquals(
					typ,
					ItemType.fromItemStack(ItemResources.loadItem(name))
				)
			}
		}
}
