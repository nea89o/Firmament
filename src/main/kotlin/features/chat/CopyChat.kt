package moe.nea.firmament.features.chat

import net.minecraft.text.OrderedText
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.reconstitute


object CopyChat : FirmamentFeature {
	override val identifier: String
		get() = "copy-chat"

	object TConfig : ManagedConfig(identifier, Category.CHAT) {
		val copyChat by toggle("copy-chat") { false }
	}

	override val config: ManagedConfig?
		get() = TConfig

	fun orderedTextToString(orderedText: OrderedText): String {
		return orderedText.reconstitute().string
	}


}
