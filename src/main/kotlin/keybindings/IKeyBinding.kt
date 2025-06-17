

package moe.nea.firmament.keybindings

import net.minecraft.client.option.KeyBinding

interface IKeyBinding {
    fun matches(keyCode: Int, scanCode: Int, modifiers: Int): Boolean
	fun matchesAtLeast(keyCode: Int, scanCode: Int, modifiers: Int): Boolean

    fun withModifiers(wantedModifiers: Int): IKeyBinding {
        val old = this
        return object : IKeyBinding {
            override fun matches(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
                return old.matchesAtLeast(keyCode, scanCode, modifiers) && (modifiers and wantedModifiers) == wantedModifiers
            }

			override fun matchesAtLeast(
				keyCode: Int,
				scanCode: Int,
				modifiers: Int
			): Boolean {
				return old.matchesAtLeast(keyCode, scanCode, modifiers) && (modifiers.inv() and wantedModifiers) == 0
			}
		}
    }

    companion object {
        fun minecraft(keyBinding: KeyBinding) = object : IKeyBinding {
            override fun matches(keyCode: Int, scanCode: Int, modifiers: Int) =
                keyBinding.matchesKey(keyCode, scanCode)

			override fun matchesAtLeast(
				keyCode: Int,
				scanCode: Int,
				modifiers: Int
			): Boolean =
				keyBinding.matchesKey(keyCode, scanCode)
		}

        fun ofKeyCode(wantedKeyCode: Int) = object : IKeyBinding {
            override fun matches(keyCode: Int, scanCode: Int, modifiers: Int): Boolean = keyCode == wantedKeyCode && modifiers == 0
			override fun matchesAtLeast(
				keyCode: Int,
				scanCode: Int,
				modifiers: Int
			): Boolean = keyCode == wantedKeyCode
		}
    }
}
