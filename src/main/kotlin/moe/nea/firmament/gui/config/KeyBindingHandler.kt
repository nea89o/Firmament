package moe.nea.firmament.gui.config

import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.data.InputResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import moe.nea.firmament.keybindings.FirmamentKeyBindings
import moe.nea.firmament.keybindings.SavedKeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.lwjgl.glfw.GLFW

class KeyBindingHandler(name: String, managedConfig: ManagedConfig) : ManagedConfig.OptionHandler<SavedKeyBinding> {
    init {
        FirmamentKeyBindings.registerKeyBinding(name, managedConfig)
    }

    override fun toJson(element: SavedKeyBinding): JsonElement? {
        return Json.encodeToJsonElement(element)
    }

    override fun fromJson(element: JsonElement): SavedKeyBinding {
        return Json.decodeFromJsonElement(element)
    }

    override fun emitGuiElements(opt: ManagedOption<SavedKeyBinding>, guiAppender: GuiAppender) {
        var editing = false
        var lastPressed = 0
        var lastPressedNonModifier = 0
        var updateButton: (() -> Unit)? = null
        val button = object : WButton() {
            override fun onKeyPressed(ch: Int, key: Int, modifiers: Int): InputResult {
                if (!editing) {
                    return super.onKeyPressed(ch, key, modifiers)
                }
                if (ch == GLFW.GLFW_KEY_ESCAPE) {
                    lastPressedNonModifier = 0
                    editing = false
                    lastPressed = 0
                    updateButton!!()
                    return InputResult.PROCESSED
                }
                if (ch == GLFW.GLFW_KEY_LEFT_SHIFT || ch == GLFW.GLFW_KEY_RIGHT_SHIFT
                    || ch == GLFW.GLFW_KEY_LEFT_ALT || ch == GLFW.GLFW_KEY_RIGHT_ALT
                    || ch == GLFW.GLFW_KEY_LEFT_CONTROL || ch == GLFW.GLFW_KEY_RIGHT_CONTROL
                ) {
                    lastPressed = ch
                } else {
                    opt.value = SavedKeyBinding(
                        ch, modifiers
                    )
                    editing = false
                    lastPressed = 0
                    lastPressedNonModifier = 0
                }
                updateButton!!()
                return InputResult.PROCESSED
            }

            override fun onFocusLost() {
                super.onFocusLost()
                lastPressedNonModifier = 0
                editing = false
                lastPressed = 0
                updateButton!!()
            }

            override fun onKeyReleased(ch: Int, key: Int, modifiers: Int): InputResult {
                if (!editing)
                    return super.onKeyReleased(ch, key, modifiers)
                if (lastPressedNonModifier == ch || (lastPressedNonModifier == 0 && ch == lastPressed)) {
                    opt.value = SavedKeyBinding(
                        ch, modifiers
                    )
                    editing = false
                    lastPressed = 0
                    lastPressedNonModifier = 0
                }
                updateButton!!()
                return InputResult.PROCESSED
            }
        }

        fun updateLabel() {
            val stroke = Text.literal("")
            if (opt.value.shift) {
                stroke.append("SHIFT + ") // TODO: translations?
            }
            if (opt.value.alt) {
                stroke.append("ALT + ")
            }
            if (opt.value.ctrl) {
                stroke.append("CTRL + ")
            }
            stroke.append(InputUtil.Type.KEYSYM.createFromCode(opt.value.keyCode).localizedText)
            if (editing)
                stroke.styled { it.withColor(Formatting.YELLOW) }
            button.setLabel(stroke)
        }
        updateButton = ::updateLabel
        updateButton()
        button.setOnClick {
            editing = true
            button.requestFocus()
            updateButton()
        }
        guiAppender.appendLabeledRow(opt.labelText, button)
    }

}
