package moe.nea.firmament.util.skyblock

import org.intellij.lang.annotations.Language
import net.minecraft.text.Text
import moe.nea.firmament.util.StringUtil.title
import moe.nea.firmament.util.StringUtil.unwords
import moe.nea.firmament.util.mc.MCTabListAPI
import moe.nea.firmament.util.unformattedString

object TabListAPI {

	fun getWidgetLines(widgetName: WidgetName, includeTitle: Boolean = false, from: MCTabListAPI.CurrentTabList = MCTabListAPI.currentTabList): List<Text> {
		return from.body
			.dropWhile { !widgetName.matchesTitle(it) }
			.takeWhile { it.string.isNotBlank() && !it.string.startsWith("               ") }
			.let { if (includeTitle) it else it.drop(1) }
	}

	enum class WidgetName(regex: Regex?) {
		COMMISSIONS,
		SKILLS("Skills:( .*)?"),
		PROFILE("Profile: (.*)"),
		COLLECTION,
		ESSENCE,
		PET
		;

		fun matchesTitle(it: Text): Boolean {
			return regex.matches(it.unformattedString)
		}

		constructor() : this(null)
		constructor(@Language("RegExp") regex: String) : this(Regex(regex))

		val label =
			name.split("_").map { it.lowercase().title() }.unwords()
		val regex = regex ?: Regex.fromLiteral("$label:")

	}

}
