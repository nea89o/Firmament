package moe.nea.firmament.features.macros

import kotlin.time.Duration.Companion.seconds
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HudRenderEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.events.WorldKeyboardEvent
import moe.nea.firmament.keybindings.SavedKeyBinding
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TimeMark

object ComboProcessor {

	var rootTrie: Branch = Branch(mapOf())
		private set

	var activeTrie: Branch = rootTrie
		private set

	var isInputting = false
	var lastInput = TimeMark.farPast()
	val breadCrumbs = mutableListOf<SavedKeyBinding>()
	// TODO: keep breadcrumbs


	init {
		val f = SavedKeyBinding(InputUtil.GLFW_KEY_F)
		val one = SavedKeyBinding(InputUtil.GLFW_KEY_1)
		val two = SavedKeyBinding(InputUtil.GLFW_KEY_2)
		setActions(
			listOf(
				ComboKeyAction(CommandAction("wardrobe"), listOf(f, one)),
				ComboKeyAction(CommandAction("equipment"), listOf(f, two)),
			)
		)
	}

	fun setActions(actions: List<ComboKeyAction>) {
		rootTrie = KeyComboTrie.fromComboList(actions)
		reset()
	}

	fun reset() {
		activeTrie = rootTrie
		lastInput = TimeMark.now()
		isInputting = false
		breadCrumbs.clear()
	}

	@Subscribe
	fun onTick(event: TickEvent) {
		if (isInputting && lastInput.passedTime() > 3.seconds)
			reset()
	}


	@Subscribe
	fun onRender(event: HudRenderEvent) {
		if (!isInputting) return
		if (!event.isRenderingHud) return
		event.context.matrices.push()
		val width = 120
		event.context.matrices.translate(
			(MC.window.scaledWidth - width) / 2F,
			(MC.window.scaledHeight) / 2F + 8,
			0F
		)
		val breadCrumbText = breadCrumbs.joinToString(" > ")
		event.context.drawText(MC.font, breadCrumbText, 0, 0, -1, true)
		event.context.matrices.translate(0F, MC.font.fontHeight + 2F, 0F)
		for ((key, value) in activeTrie.nodes) {
			event.context.drawText(MC.font, Text.literal("$breadCrumbText > $key: ").append(value.label), 0, 0, -1, true)
			event.context.matrices.translate(0F, MC.font.fontHeight + 1F, 0F)
		}
		event.context.matrices.pop()
	}

	@Subscribe
	fun onKeyBinding(event: WorldKeyboardEvent) {
		val nextEntry = activeTrie.nodes.entries
			.find { event.matches(it.key) }
		if (nextEntry == null) {
			reset()
			return
		}
		event.cancel()
		breadCrumbs.add(nextEntry.key)
		lastInput = TimeMark.now()
		isInputting = true
		val value = nextEntry.value
		when (value) {
			is Branch -> {
				activeTrie = value
			}

			is Leaf -> {
				value.execute()
				reset()
			}
		}.let { }
	}
}
