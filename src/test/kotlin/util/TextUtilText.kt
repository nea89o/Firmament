package moe.nea.firmament.test.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import moe.nea.firmament.test.testutil.ItemResources
import moe.nea.firmament.util.getLegacyFormatString

class TextUtilText {
	@Test
	fun testThing() {
		// TODO: add more tests that are directly validated with 1.8.9 code
		val text = ItemResources.loadText("all-chat")
		Assertions.assertEquals(
			"§r§r§8[§r§9302§r§8] §r§6♫ §r§b[MVP§r§d+§r§b] lrg89§r§f: test§r",
			text.getLegacyFormatString()
		)
	}
}
