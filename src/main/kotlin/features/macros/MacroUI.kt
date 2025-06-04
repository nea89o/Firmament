package moe.nea.firmament.features.macros

import io.github.notenoughupdates.moulconfig.gui.CloseEventListener
import io.github.notenoughupdates.moulconfig.observer.ObservableList
import io.github.notenoughupdates.moulconfig.xml.Bind
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.gui.config.AllConfigsGui.toObservableList
import moe.nea.firmament.gui.config.KeyBindingStateManager
import moe.nea.firmament.keybindings.SavedKeyBinding
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.MoulConfigUtils
import moe.nea.firmament.util.ScreenUtil

class MacroUI {


	companion object {
		@Subscribe
		fun onCommands(event: CommandEvent.SubCommand) {
			// TODO: add button in config
			event.subcommand("macros") {
				thenExecute {
					ScreenUtil.setScreenLater(MoulConfigUtils.loadScreen("config/macros/index", MacroUI(), null))
				}
			}
		}

	}

	@field:Bind("combos")
	val combos = Combos()

	class Combos {
		@field:Bind("actions")
		val actions: ObservableList<ActionEditor> = ObservableList(
			MacroData.DConfig.data.comboActions.mapTo(mutableListOf()) {
				ActionEditor(it, this)
			}
		)

		var dontSave = false

		@Bind
		fun beforeClose(): CloseEventListener.CloseAction {
			if (!dontSave)
				save()
			return CloseEventListener.CloseAction.NO_OBJECTIONS_TO_CLOSE
		}

		@Bind
		fun addCommand() {
			actions.add(
				ActionEditor(
					ComboKeyAction(
						CommandAction("ac Hello from a Firmament Hotkey"),
						listOf()
					),
					this
				)
			)
		}

		@Bind
		fun discard() {
			dontSave = true
			MC.screen?.close()
		}

		@Bind
		fun saveAndClose() {
			save()
			MC.screen?.close()
		}

		@Bind
		fun save() {
			MacroData.DConfig.data.comboActions = actions.map { it.asSaveable() }
			MacroData.DConfig.markDirty()
			ComboProcessor.setActions(MacroData.DConfig.data.comboActions) // TODO: automatically reload those from the config on startup
		}
	}

	class KeyBindingEditor(var binding: SavedKeyBinding, val parent: ActionEditor) {
		val sm = KeyBindingStateManager(
			{ binding },
			{ binding = it },
			::blur,
			::requestFocus
		)

		@field:Bind
		val button = sm.createButton()

		init {
			sm.updateLabel()
		}

		fun blur() {
			button.blur()
		}

		@Bind
		fun delete() {
			parent.combo.removeIf { it === this }
			parent.combo.update()
		}

		fun requestFocus() {
			button.requestFocus()
		}
	}

	class ActionEditor(val action: ComboKeyAction, val parent: MacroUI.Combos) {
		fun asSaveable(): ComboKeyAction {
			return ComboKeyAction(
				CommandAction(command),
				combo.map { it.binding }
			)
		}

		@field:Bind("command")
		var command: String = (action.action as CommandAction).command

		@field:Bind("combo")
		val combo = action.keys.map { KeyBindingEditor(it, this) }.toObservableList()

		@Bind
		fun formattedCombo() =
			combo.joinToString(" > ") { it.binding.toString() }

		@Bind
		fun addStep() {
			combo.add(KeyBindingEditor(SavedKeyBinding.unbound(), this))
		}

		@Bind
		fun back() {
			MC.screen?.close()
		}

		@Bind
		fun delete() {
			parent.actions.removeIf { it === this }
			parent.actions.update()
		}
		@Bind
		fun edit() {
			MC.screen = MoulConfigUtils.loadScreen("config/macros/editor", this, MC.screen)
		}
	}
}

private fun <T> ObservableList<T>.setAll(ts: Collection<T>) {
	val observer = this.observer
	this.clear()
	this.addAll(ts)
	this.observer = observer
	this.update()
}
