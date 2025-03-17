package moe.nea.firmament.util.skyblock.stats

import net.minecraft.util.Formatting

enum class BuffKind(
	val color: Formatting,
	val prefix: String,
	val postFix: String,
	val isHidden: Boolean,
) {
	REFORGE(Formatting.BLUE, "(", ")", false),
	STAR_BUFF(Formatting.RESET, "", "", true),
	CATA_STAR_BUFF(Formatting.DARK_GRAY, "(", ")", false),
	;
}
