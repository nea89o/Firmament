package moe.nea.firmament.features.chat

import net.minecraft.text.OrderedText
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig


object CopyChat : FirmamentFeature {
	override val identifier: String
		get() = "copy-chat"

	object TConfig : ManagedConfig(identifier, Category.CHAT) {
		val copyChat by toggle("copy-chat") { false }
	}

	override val config: ManagedConfig?
		get() = TConfig

	fun orderedTextToString(orderedText: OrderedText): String {
		val sb = StringBuilder()
		orderedText.accept { _, _, codePoint ->
			sb.appendCodePoint(codePoint)
			true
		}
		return sb.toString()
	}


}
