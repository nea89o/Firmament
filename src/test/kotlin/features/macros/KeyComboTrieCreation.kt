package moe.nea.firmament.test.features.macros

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import net.minecraft.client.util.InputUtil
import moe.nea.firmament.features.macros.Branch
import moe.nea.firmament.features.macros.ComboKeyAction
import moe.nea.firmament.features.macros.CommandAction
import moe.nea.firmament.features.macros.KeyComboTrie
import moe.nea.firmament.features.macros.Leaf
import moe.nea.firmament.keybindings.SavedKeyBinding

class KeyComboTrieCreation {
	val basicAction = CommandAction("ac Hello")
	val aPress = SavedKeyBinding(InputUtil.GLFW_KEY_A)
	val bPress = SavedKeyBinding(InputUtil.GLFW_KEY_B)
	val cPress = SavedKeyBinding(InputUtil.GLFW_KEY_C)

	@Test
	fun testValidShortTrie() {
		val actions = listOf(
			ComboKeyAction(basicAction, listOf(aPress)),
			ComboKeyAction(basicAction, listOf(bPress)),
			ComboKeyAction(basicAction, listOf(cPress)),
		)
		Assertions.assertEquals(
			Branch(
				mapOf(
					aPress to Leaf(basicAction),
					bPress to Leaf(basicAction),
					cPress to Leaf(basicAction),
				),
			), KeyComboTrie.fromComboList(actions)
		)
	}

	@Test
	fun testOverlappingLeafs() {
		Assertions.assertThrows(IllegalStateException::class.java) {
			KeyComboTrie.fromComboList(
				listOf(
					ComboKeyAction(basicAction, listOf(aPress, aPress)),
					ComboKeyAction(basicAction, listOf(aPress, aPress)),
				)
			)
		}
		Assertions.assertThrows(IllegalStateException::class.java) {
			KeyComboTrie.fromComboList(
				listOf(
					ComboKeyAction(basicAction, listOf(aPress)),
					ComboKeyAction(basicAction, listOf(aPress)),
				)
			)
		}
	}

	@Test
	fun testBranchOverlappingLeaf() {
		Assertions.assertThrows(IllegalStateException::class.java) {
			KeyComboTrie.fromComboList(
				listOf(
					ComboKeyAction(basicAction, listOf(aPress)),
					ComboKeyAction(basicAction, listOf(aPress, aPress)),
				)
			)
		}
	}
	@Test
	fun testLeafOverlappingBranch() {
		Assertions.assertThrows(IllegalStateException::class.java) {
			KeyComboTrie.fromComboList(
				listOf(
					ComboKeyAction(basicAction, listOf(aPress, aPress)),
					ComboKeyAction(basicAction, listOf(aPress)),
				)
			)
		}
	}


	@Test
	fun testValidNestedTrie() {
		val actions = listOf(
			ComboKeyAction(basicAction, listOf(aPress, aPress)),
			ComboKeyAction(basicAction, listOf(aPress, bPress)),
			ComboKeyAction(basicAction, listOf(cPress)),
		)
		Assertions.assertEquals(
			Branch(
				mapOf(
					aPress to Branch(
						mapOf(
							aPress to Leaf(basicAction),
							bPress to Leaf(basicAction),
						)
					),
					cPress to Leaf(basicAction),
				),
			), KeyComboTrie.fromComboList(actions)
		)
	}

}
