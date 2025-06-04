package moe.nea.firmament.features.macros

import kotlinx.serialization.Serializable
import net.minecraft.text.Text
import moe.nea.firmament.keybindings.SavedKeyBinding
import moe.nea.firmament.util.ErrorUtil

sealed interface KeyComboTrie {
	val label: Text

	companion object {
		fun fromComboList(
			combos: List<ComboKeyAction>,
		): Branch {
			val root = Branch(mutableMapOf())
			for (combo in combos) {
				var p = root
				if (combo.keys.isEmpty()) {
					ErrorUtil.softUserError("Key Combo for ${combo.action.label.string} is empty")
					continue
				}
				for ((index, key) in combo.keys.withIndex()) {
					val m = (p.nodes as MutableMap)
					if (index == combo.keys.lastIndex) {
						if (key in m) {
							ErrorUtil.softUserError("Overlapping actions found for ${combo.keys.joinToString(" > ")} (another action ${m[key]} already exists).")
							break
						}

						m[key] = Leaf(combo.action)
					} else {
						val c = m.getOrPut(key) { Branch(mutableMapOf()) }
						if (c !is Branch) {
							ErrorUtil.softUserError("Overlapping actions found for ${combo.keys} (final node exists at index $index) through another action already")
							break
						} else {
							p = c
						}
					}
				}
			}
			return root
		}
	}
}


@Serializable
data class ComboKeyAction(
	val action: HotkeyAction,
	val keys: List<SavedKeyBinding>,
)

data class Leaf(val action: HotkeyAction) : KeyComboTrie {
	override val label: Text
		get() = action.label

	fun execute() {
		action.execute()
	}
}

data class Branch(
	val nodes: Map<SavedKeyBinding, KeyComboTrie>
) : KeyComboTrie {
	override val label: Text
		get() = Text.literal("...") // TODO: better labels
}
