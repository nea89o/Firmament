package moe.nea.firmament.test.util.skyblock

import io.kotest.core.spec.style.AnnotationSpec
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import moe.nea.firmament.test.testutil.ItemResources
import moe.nea.firmament.util.skyblock.ItemType

class ItemTypeTest : AnnotationSpec() {
	@Test
	fun testPetItem() {
		Assertions.assertEquals(
			ItemType.PET,
			ItemType.fromItemStack(ItemResources.loadItem("pets/lion-item"))
		)
	}

	@Test
	fun testPetInUI() {
		Assertions.assertEquals(
			ItemType.PET,
			ItemType.fromItemStack(ItemResources.loadItem("pets/rabbit-selected"))
		)
		Assertions.assertEquals(
			ItemType.PET,
			ItemType.fromItemStack(ItemResources.loadItem("pets/mithril-golem-not-selected"))
		)
	}

	@Test
	fun testAOTV() {
		Assertions.assertEquals(
			ItemType.SWORD,
			ItemType.fromItemStack(ItemResources.loadItem("aspect-of-the-void"))
		)
	}

	@Test
	fun testDrill() {
		Assertions.assertEquals(
			ItemType.DRILL,
			ItemType.fromItemStack(ItemResources.loadItem("titanium-drill"))
		)
	}

	@Test
	fun testPickaxe() {
		Assertions.assertEquals(
			ItemType.PICKAXE,
			ItemType.fromItemStack(ItemResources.loadItem("diamond-pickaxe"))
		)
	}
}
