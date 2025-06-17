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

	@field:Bind("wheels")
	val wheels = Wheels()
	var dontSave = false

	@Bind
	fun beforeClose(): CloseEventListener.CloseAction {
		if (!dontSave)
			save()
		return CloseEventListener.CloseAction.NO_OBJECTIONS_TO_CLOSE
	}

	fun save() {
		MacroData.DConfig.data.comboActions = combos.actions.map { it.asSaveable() }
		MacroData.DConfig.data.wheels = wheels.wheels.map { it.asSaveable() }
		MacroData.DConfig.markDirty()
		RadialMacros.setWheels(MacroData.DConfig.data.wheels)
		ComboProcessor.setActions(MacroData.DConfig.data.comboActions)
	}

	fun discard() {
		dontSave = true
		MC.screen?.close()
	}

	class Command(
		@field:Bind("text")
		var text: String,
		val parent: Wheel,
	) {
		@Bind
		fun delete() {
			parent.editableCommands.removeIf { it === this }
			parent.editableCommands.update()
			parent.commands.update()
		}

		fun asCommandAction() = CommandAction(text)
	}

	inner class Wheel(
		val parent: Wheels,
		var binding: SavedKeyBinding,
		commands: List<CommandAction>,
	) {

		fun asSaveable(): MacroWheel {
			return MacroWheel(binding, commands.map { it.asCommandAction() })
		}

		@Bind("keyCombo")
		fun text() = binding.format().string

		@field:Bind("commands")
		val commands = commands.mapTo(ObservableList(mutableListOf())) { Command(it.command, this) }

		@field:Bind("editableCommands")
		val editableCommands = this.commands.toObservableList()

		@Bind
		fun addOption() {
			editableCommands.add(Command("", this))
		}

		@Bind
		fun back() {
			MC.screen?.close()
		}

		@Bind
		fun edit() {
			MC.screen = MoulConfigUtils.loadScreen("config/macros/editor_wheel", this, MC.screen)
		}

		@Bind
		fun delete() {
			parent.wheels.removeIf { it === this }
			parent.wheels.update()
		}

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


		fun requestFocus() {
			button.requestFocus()
		}
	}

	inner class Wheels {
		@field:Bind("wheels")
		val wheels: ObservableList<Wheel> = MacroData.DConfig.data.wheels.mapTo(ObservableList(mutableListOf())) {
			Wheel(this, it.key, it.options.map { CommandAction((it as CommandAction).command) })
		}

		@Bind
		fun discard() {
			this@MacroUI.discard()
		}

		@Bind
		fun saveAndClose() {
			this@MacroUI.saveAndClose()
		}

		@Bind
		fun save() {
			this@MacroUI.save()
		}

		@Bind
		fun addWheel() {
			wheels.add(Wheel(this, SavedKeyBinding.unbound(), listOf()))
		}
	}

	fun saveAndClose() {
		save()
		MC.screen?.close()
	}

	inner class Combos {
		@field:Bind("actions")
		val actions: ObservableList<ActionEditor> = ObservableList(
			MacroData.DConfig.data.comboActions.mapTo(mutableListOf()) {
				ActionEditor(it, this)
			}
		)

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
			this@MacroUI.discard()
		}

		@Bind
		fun saveAndClose() {
			this@MacroUI.saveAndClose()
		}

		@Bind
		fun save() {
			this@MacroUI.save()
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


		fun requestFocus() {
			button.requestFocus()
		}

		@Bind
		fun delete() {
			parent.combo.removeIf { it === this }
			parent.combo.update()
		}
	}

	class ActionEditor(val action: ComboKeyAction, val parent: Combos) {
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
			MC.screen = MoulConfigUtils.loadScreen("config/macros/editor_combo", this, MC.screen)
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
