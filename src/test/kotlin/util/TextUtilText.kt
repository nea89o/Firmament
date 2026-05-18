package moe.nea.firmament.test.util

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
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

	@Test
	fun `legacy formatting inherits parent style`() {
		val text = Component.literal("")
			.withStyle { it.withItalic(false) }
			.append(
				Component.literal("X")
					.withStyle(ChatFormatting.DARK_PURPLE)
					.withStyle { it.withObfuscated(true) }
			)
			.append(
				Component.literal(" Rift-Transferable ")
					.withStyle(ChatFormatting.DARK_PURPLE)
					.append(
						Component.literal("X")
							.withStyle { it.withObfuscated(true) }
					)
			)

		Assertions.assertEquals(
			"§5§kX§5 Rift-Transferable §5§kX",
			text.getLegacyFormatString(trimmed = true)
		)
	}
}
