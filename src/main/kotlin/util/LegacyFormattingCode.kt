package moe.nea.firmament.util

import net.minecraft.util.Formatting

enum class LegacyFormattingCode(val label: String, val char: Char, val index: Int) {
	BLACK("BLACK", '0', 0),
	DARK_BLUE("DARK_BLUE", '1', 1),
	DARK_GREEN("DARK_GREEN", '2', 2),
	DARK_AQUA("DARK_AQUA", '3', 3),
	DARK_RED("DARK_RED", '4', 4),
	DARK_PURPLE("DARK_PURPLE", '5', 5),
	GOLD("GOLD", '6', 6),
	GRAY("GRAY", '7', 7),
	DARK_GRAY("DARK_GRAY", '8', 8),
	BLUE("BLUE", '9', 9),
	GREEN("GREEN", 'a', 10),
	AQUA("AQUA", 'b', 11),
	RED("RED", 'c', 12),
	LIGHT_PURPLE("LIGHT_PURPLE", 'd', 13),
	YELLOW("YELLOW", 'e', 14),
	WHITE("WHITE", 'f', 15),
	OBFUSCATED("OBFUSCATED", 'k', -1),
	BOLD("BOLD", 'l', -1),
	STRIKETHROUGH("STRIKETHROUGH", 'm', -1),
	UNDERLINE("UNDERLINE", 'n', -1),
	ITALIC("ITALIC", 'o', -1),
	RESET("RESET", 'r', -1);

	companion object {
		val byCode = entries.associateBy { it.char }
	}

	val modern = Formatting.byCode(char)!!

	val formattingCode = "§$char"

}
