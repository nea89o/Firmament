package moe.nea.firmament.test.util.skyblock

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import moe.nea.firmament.util.skyblock.EnchantedBookLore

class EnchantedBookLoreTest {
	@Test
	fun testOldLoreStyle() {
		Assertions.assertEquals(
			"§9Feather Falling VI",
			EnchantedBookLore.findEnchantmentName(
				listOf(
					"§9Feather Falling VI",
					"§7Increases how high you can fall",
					"§7before taking fall damage by",
					"§a6§7 and reduces fall damage by",
					"§a30%§7.",
					"",
					"§9§lRARE",
				)
			)
		)
	}

	@Test
	fun testNewLoreStyle() {
		Assertions.assertEquals(
			"§9Feather Falling VI",
			EnchantedBookLore.findEnchantmentName(
				listOf(
					"§8Combinable in Anvil",
					"",
					"§9Feather Falling VI",
					"§7Increases how high you can fall",
					"§7before taking fall damage by",
					"§a6§7 and reduces fall damage by",
					"§a30%§7.",
					"",
					"§9§lRARE",
				)
			)
		)
	}

	@Test
	fun testUltimateEnchantmentInNewLoreStyle() {
		Assertions.assertEquals(
			"§d§lUltimate Wise I",
			EnchantedBookLore.findEnchantmentName(
				listOf(
					"§8Combinable in Anvil",
					"",
					"§d§lUltimate Wise I",
					"§7Reduces the ability mana cost of",
					"§7this item by §a10%§7.",
					"",
					"§f§lCOMMON",
				)
			)
		)
	}

	@Test
	fun testExtraBlankLinesAfterHeader() {
		Assertions.assertEquals(
			"§9Sharpness V",
			EnchantedBookLore.findEnchantmentName(
				listOf("§8Combinable in Anvil", "", "   ", "§9Sharpness V")
			)
		)
	}

	@Test
	fun testHeaderWithoutColorCodes() {
		Assertions.assertEquals(
			"§9Sharpness V",
			EnchantedBookLore.findEnchantmentName(listOf("Combinable in Anvil", "", "§9Sharpness V"))
		)
	}

	@Test
	fun testEmptyLore() {
		Assertions.assertNull(EnchantedBookLore.findEnchantmentName(listOf<String>()))
	}

	@Test
	fun testHeaderOnly() {
		Assertions.assertNull(EnchantedBookLore.findEnchantmentName(listOf("§8Combinable in Anvil", "")))
	}
}
