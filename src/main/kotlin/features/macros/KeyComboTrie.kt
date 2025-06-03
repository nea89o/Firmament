package moe.nea.firmament.features.macros

import net.minecraft.text.Text
import moe.nea.firmament.keybindings.SavedKeyBinding

sealed interface KeyComboTrie {
	val label: Text

	companion object {
		fun fromComboList(
			combos: List<ComboKeyAction>,
		): Branch {
			val root = Branch(mutableMapOf())
			for (combo in combos) {
				var p = root
				require(combo.keys.isNotEmpty())
				for ((index, key) in combo.keys.withIndex()) {
					val m = (p.nodes as MutableMap)
					if (index == combo.keys.lastIndex) {
						if (key in m)
							error("Overlapping actions found for ${combo.keys} (another action ${m[key]} already exists).")

						m[key] = Leaf(combo.action)
					} else {
						val c = m.getOrPut(key) { Branch(mutableMapOf()) }
						if (c !is Branch)
							error("Overlapping actions found for ${combo.keys} (final node exists at index $index) through another action already")
						p = c
					}
				}
			}
			return root
		}
	}
}


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
