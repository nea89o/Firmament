package moe.nea.firmament.test.util.skyblock

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import moe.nea.firmament.test.testutil.ItemResources
import moe.nea.firmament.util.skyblock.ItemType

class ItemTypeTest
	: ShouldSpec(
	{
		context("ItemType.fromItemstack") {
			listOf(
				"pets/lion-item" to ItemType.PET,
				"pets/rabbit-selected" to ItemType.PET,
				"pets/mithril-golem-not-selected" to ItemType.PET,
				"aspect-of-the-void" to ItemType.SWORD,
				"titanium-drill" to ItemType.DRILL,
				"diamond-pickaxe" to ItemType.PICKAXE,
				"gemstone-gauntlet" to ItemType.GAUNTLET,
			).forEach { (name, typ) ->
				should("return $typ for $name") {
					ItemType.fromItemStack(ItemResources.loadItem(name)) shouldBe typ
				}
			}
		}
	})
