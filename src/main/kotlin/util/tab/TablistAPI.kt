package moe.nea.firmament.util.tab

// Skidded from NEU :broken_heart:
// Credit: https://github.com/NotEnoughUpdates/NotEnoughUpdates/blob/24ff8316f1b43db4dca47bb1fba529e69d0d72df/src/main/kotlin/io/github/moulberry/notenoughupdates/miscfeatures/tablisttutorial/TablistAPI.kt (modified)

import java.util.Locale
import moe.nea.firmament.util.removeColorCodes

object TablistAPI {

	fun getWidgetLinesInRegion(widgetName: WidgetNames): List<String> {
		val regex = widgetName.regex ?: Regex.fromLiteral("${widgetName}:")
		val list = mutableListOf<String>()
		// If not a single reset is present, the tab list hasn't been initialized yet
		var sawReset = false

		for (entry in TablistUtils.getTabList()) {
			if (entry.contains("§r")) {
				sawReset = true
			}

			if (list.isNotEmpty()) {
				// Empty line
				// Or there is no spacing between two widgets
				// The widget ends here.
				if (entry == "§r" || entry.startsWith("§r§"))break

				// New tab column, ignore it and continue with the same widget
				if (entry == "§r               §r§3§lInfo§r") continue

				list.add(entry)
			} else if (entry.removeColorCodes().matches(regex)) {
				list.add(entry)
			}
		}

		return list
	}
}


enum class WidgetNames(val regex: Regex?) {
	COMMISSIONS(null),
	/*
		'§e§lSkills:'
		' Farming 50: §r§a43.3%'
		' Mining 60: §r§c§lMAX'
		' Combat 46: §r§a21.7%'
		' Foraging 23: §r§a43.5%'
	* */
	SKILLS(Regex("Skills:( .*)?")),
	/*
	* '§e§lSkills: §r§aCombat 46: §r§321.7%'
	* */
	DUNGEON_SKILLS(Regex("Skills: (.*)")),
	TRAPPER(null),
	FORGE(Regex("Forges:( \\(\\d/\\d\\))?")),
	POWDER(Regex.fromLiteral("Powders:")),
	PROFILE(Regex("Profile: ([A-Za-z]+)( .*)?")),
	ACTIVE_EFFECTS(Regex("Active Effects(: \\(\\d+\\))?")),
	COOKIE_BUFF(Regex("Cookie Buff")),
	PET(null),
	;

	override fun toString(): String {
		return this.name.lowercase().split("_").joinToString(" ") { str ->
			str.replaceFirstChar {
				if (it.isLowerCase()) {
					it.titlecase(
						Locale.ROOT
					)
				} else {
					it.toString()
				}
			}
		}
	}
}
