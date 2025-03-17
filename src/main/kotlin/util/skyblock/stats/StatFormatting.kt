package util.skyblock.stats

import net.minecraft.util.Formatting

data class StatFormatting(
	val name: String,
	val postFix: String,
	val color: Formatting,
	val isStarAffected: Boolean = true,
) {
	companion object {
		fun statIdToName(statId: String): String {
			val segments = statId.split("_")
			return segments.joinToString(" ") { it.replaceFirstChar { it.uppercaseChar() } }
		}
		fun findForName(name: String) =
			formattingOverrides[name] ?: StatFormatting(name, "", Formatting.GREEN)

		fun findForId(id: String) = findForName(statIdToName(id))

		val allFormattingOverrides = listOf(
			StatFormatting("Sea Creature Chance", "%", Formatting.RED),
			StatFormatting("Strength", "", Formatting.RED),
			StatFormatting("Damage", "", Formatting.RED),
			StatFormatting("Bonus Attack Speed", "%", Formatting.RED),
			StatFormatting("Shot Cooldown", "s", Formatting.GREEN, false),
			StatFormatting("Ability Damage", "%", Formatting.RED),
			StatFormatting("Crit Damage", "%", Formatting.RED),
			StatFormatting("Crit Chance", "%", Formatting.RED),
			StatFormatting("Ability Damage", "%", Formatting.RED),
			StatFormatting("Trophy Fish Chance", "%", Formatting.GREEN),
			StatFormatting("Health", "", Formatting.GREEN),
			StatFormatting("Defense", "", Formatting.GREEN),
			StatFormatting("Fishing Speed", "", Formatting.GREEN),
			StatFormatting("Double Hook Chance", "%", Formatting.GREEN),
			StatFormatting("Mining Speed", "", Formatting.GREEN),
			StatFormatting("Mining Fortune", "", Formatting.GREEN),
			StatFormatting("Heat Resistance", "", Formatting.GREEN),
			StatFormatting("Swing Range", "", Formatting.GREEN),
			StatFormatting("Rift Time", "", Formatting.GREEN),
			StatFormatting("Speed", "", Formatting.GREEN),
			StatFormatting("Farming Fortune", "", Formatting.GREEN),
			StatFormatting("True Defense", "", Formatting.GREEN),
			StatFormatting("Mending", "", Formatting.GREEN),
			StatFormatting("Foraging Wisdom", "", Formatting.GREEN),
			StatFormatting("Farming Wisdom", "", Formatting.GREEN),
			StatFormatting("Foraging Fortune", "", Formatting.GREEN),
			StatFormatting("Magic Find", "", Formatting.GREEN),
			StatFormatting("Ferocity", "", Formatting.GREEN),
			StatFormatting("Bonus Pest Chance", "%", Formatting.GREEN),
			StatFormatting("Cold Resistance", "", Formatting.GREEN),
			StatFormatting("Pet Luck", "", Formatting.GREEN),
			StatFormatting("Fear", "", Formatting.GREEN),
			StatFormatting("Mana Regen", "%", Formatting.GREEN),
			StatFormatting("Rift Damage", "", Formatting.GREEN),
			StatFormatting("Hearts", "", Formatting.GREEN),
			StatFormatting("Vitality", "", Formatting.GREEN),
			// TODO: make this a repo json
		)
		val formattingOverrides = allFormattingOverrides.associateBy { it.name }

	}
}
