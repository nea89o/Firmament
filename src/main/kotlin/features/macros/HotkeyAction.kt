package moe.nea.firmament.features.macros

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.text.Text
import moe.nea.firmament.util.MC

@Serializable
sealed interface HotkeyAction {
	// TODO: execute
	val label: Text
	fun execute()
}

@Serializable
@SerialName("command")
data class CommandAction(val command: String) : HotkeyAction {
	override val label: Text
		get() = Text.literal("/$command")

	override fun execute() {
		MC.sendCommand(command)
	}
}

// Mit onscreen anzeige:
// F -> 1 /equipment
// F -> 2 /wardrobe
// Bei Combos: Keys buffern! (für wardrobe hotkeys beispielsweiße)

// Radial menu
// Hold F
// Weight (mach eins doppelt so groß)
// /equipment
// /wardrobe

// Bei allen: Filter!
// - Nur in Dungeons / andere Insel
// - Nur wenn ich Item X im inventar habe (fishing rod)

