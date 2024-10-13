package moe.nea.firmament.gui.config

import io.github.notenoughupdates.moulconfig.common.IMinecraft
import io.github.notenoughupdates.moulconfig.common.MyResourceLocation
import io.github.notenoughupdates.moulconfig.deps.libninepatch.NinePatch
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext
import io.github.notenoughupdates.moulconfig.gui.KeyboardEvent
import io.github.notenoughupdates.moulconfig.gui.component.TextComponent
import org.lwjgl.glfw.GLFW
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import moe.nea.firmament.gui.FirmButtonComponent
import moe.nea.firmament.keybindings.FirmamentKeyBindings
import moe.nea.firmament.keybindings.SavedKeyBinding

class KeyBindingHandler(val name: String, val managedConfig: ManagedConfig) :
	ManagedConfig.OptionHandler<SavedKeyBinding> {

	override fun initOption(opt: ManagedOption<SavedKeyBinding>) {
		FirmamentKeyBindings.registerKeyBinding(name, opt)
	}

	override fun toJson(element: SavedKeyBinding): JsonElement? {
		return Json.encodeToJsonElement(element)
	}

	override fun fromJson(element: JsonElement): SavedKeyBinding {
		return Json.decodeFromJsonElement(element)
	}

	override fun emitGuiElements(opt: ManagedOption<SavedKeyBinding>, guiAppender: GuiAppender) {
		lateinit var button: FirmButtonComponent
		val sm = KeyBindingStateManager(
			{ opt.value },
			{ opt.value = it },
			{ button.blur() },
			{ button.requestFocus() }
		)
		button = object : FirmButtonComponent(
			TextComponent(
				IMinecraft.instance.defaultFontRenderer,
				{ sm.label.string },
				130,
				TextComponent.TextAlignment.LEFT,
				false,
				false
			), action = {
				sm.onClick()
			}) {
			override fun keyboardEvent(event: KeyboardEvent, context: GuiImmediateContext): Boolean {
				if (event is KeyboardEvent.KeyPressed) {
					return sm.keyboardEvent(event.keycode, event.pressed)
				}
				return super.keyboardEvent(event, context)
			}

			override fun getBackground(context: GuiImmediateContext): NinePatch<MyResourceLocation> {
				if (sm.editing) return activeBg
				return super.getBackground(context)
			}


			override fun onLostFocus() {
				sm.onLostFocus()
			}
		}
		sm.updateLabel()
		guiAppender.appendLabeledRow(opt.labelText, button)
	}

}
