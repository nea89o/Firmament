/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.config

import io.github.notenoughupdates.moulconfig.common.IMinecraft
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext
import io.github.notenoughupdates.moulconfig.gui.KeyboardEvent
import io.github.notenoughupdates.moulconfig.gui.component.TextComponent
import org.lwjgl.glfw.GLFW
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import moe.nea.firmament.gui.FirmButtonComponent
import moe.nea.firmament.keybindings.FirmamentKeyBindings
import moe.nea.firmament.keybindings.SavedKeyBinding

class KeyBindingHandler(name: String, val managedConfig: ManagedConfig) : ManagedConfig.OptionHandler<SavedKeyBinding> {
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
        var label: String = ""
        var button: FirmButtonComponent? = null
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
            label = (stroke).string
            managedConfig.save()
        }
        button = object : FirmButtonComponent(
            TextComponent(
                IMinecraft.instance.defaultFontRenderer,
                { label },
                130,
                TextComponent.TextAlignment.LEFT,
                false,
                false
            ), action = {
                if (editing) {
                    button!!.blur()
                } else {
                    editing = true
                    button!!.requestFocus()
                    updateLabel()
                }
            }) {
            override fun keyboardEvent(event: KeyboardEvent, context: GuiImmediateContext): Boolean {
                if (event is KeyboardEvent.KeyPressed) {
                    if (event.pressed) onKeyPressed(event.keycode, SavedKeyBinding.getModInt())
                    else onKeyReleased(event.keycode, SavedKeyBinding.getModInt())
                }
                return super.keyboardEvent(event, context)
            }

            fun onKeyPressed(ch: Int, modifiers: Int): Boolean {
                if (!editing) {
                    return false
                }
                if (ch == GLFW.GLFW_KEY_ESCAPE) {
                    lastPressedNonModifier = 0
                    editing = false
                    lastPressed = 0
                    opt.value = SavedKeyBinding(GLFW.GLFW_KEY_UNKNOWN)
                    updateLabel()
                    return true
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
                updateLabel()
                return true
            }

            override fun onLostFocus() {
                lastPressedNonModifier = 0
                editing = false
                lastPressed = 0
                updateLabel()
            }

            fun onKeyReleased(ch: Int, modifiers: Int): Boolean {
                if (!editing)
                    return false
                if (lastPressedNonModifier == ch || (lastPressedNonModifier == 0 && ch == lastPressed)) {
                    opt.value = SavedKeyBinding(ch, modifiers)
                    editing = false
                    lastPressed = 0
                    lastPressedNonModifier = 0
                }
                updateLabel()
                return true
            }
        }

        guiAppender.appendLabeledRow(opt.labelText, button)
    }

}
