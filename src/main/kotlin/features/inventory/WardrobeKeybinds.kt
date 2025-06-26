package moe.nea.firmament.features.inventory

import org.lwjgl.glfw.GLFW
import net.minecraft.item.Items
import net.minecraft.screen.slot.SlotActionType
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.mc.SlotUtils.clickLeftMouseButton
import moe.nea.firmament.util.mc.SlotUtils.clickMiddleMouseButton

object WardrobeKeybinds : FirmamentFeature {
	override val identifier: String
		get() = "wardrobe-keybinds"

	object TConfig : ManagedConfig(identifier, Category.INVENTORY) {
		val wardrobeKeybinds by toggle("wardrobe-keybinds") { false }
		val slotKeybinds = (1..9).map {
			keyBinding("slot-$it") { GLFW.GLFW_KEY_0 + it }
		}
	}

	override val config: ManagedConfig?
		get() = TConfig

	val slotKeybindsWithSlot = TConfig.slotKeybinds.withIndex().map { (index, keybinding) ->
		index + 36 to keybinding
	}

	@Subscribe
	fun switchSlot(event: HandledScreenKeyPressedEvent) {
		if (MC.player == null || MC.world == null || MC.interactionManager == null) return

		val regex = Regex("Wardrobe \\([12]/2\\)")
		if (!regex.matches(event.screen.title.string)) return
		if (!TConfig.wardrobeKeybinds) return

		val slot =
			slotKeybindsWithSlot
				.find { event.matches(it.second.get()) }
				?.first ?: return

		event.cancel()

		val handler = event.screen.screenHandler
		val invSlot = handler.getSlot(slot)

		val itemStack = invSlot.stack
		if (itemStack.item != Items.PINK_DYE && itemStack.item != Items.LIME_DYE) return

		invSlot.clickLeftMouseButton(handler)
	}

}
