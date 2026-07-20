package moe.nea.firmament.features.inventory

import org.lwjgl.glfw.GLFW
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.item.Items
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.mc.SlotUtils.clickLeftMouseButton

object WardrobeKeybinds {
	@Config
	object TConfig : ManagedConfig("wardrobe-keybinds", Category.INVENTORY) {
		val wardrobeKeybinds by toggle("wardrobe-keybinds") { false }
		val changePageKeybind by keyBinding("change-page") { GLFW.GLFW_KEY_ENTER }
		val nextPage by keyBinding("next-page") { GLFW.GLFW_KEY_D }
		val previousPage by keyBinding("previous-page") { GLFW.GLFW_KEY_A }
		val slotKeybinds = (1..9).map {
			keyBinding("slot-$it") { GLFW.GLFW_KEY_0 + it }
		}
		val allowUnequipping by toggle("allow-unequipping") { true }
	}

	val slotKeybindsWithSlot = TConfig.slotKeybinds.withIndex().map { (index, keybinding) ->
		index + 36 to keybinding
	}

	@Subscribe
	fun switchSlot(event: HandledScreenKeyPressedEvent) {
		if (MC.player == null || MC.world == null || MC.interactionManager == null) return
		if (event.screen !is AbstractContainerScreen<*>) return
		if (event.isRepeat) return

		val regex = Regex("\\([0-9]+/[0-9]+\\) (?:Armor|Equipment) Sets")
		if (!regex.matches(event.screen.title.string)) return
		if (!TConfig.wardrobeKeybinds) return

		if (
			event.matches(TConfig.changePageKeybind) ||
			event.matches(TConfig.previousPage) ||
			event.matches(TConfig.nextPage)
		) {
			event.cancel()

			val handler = event.screen.menu
			val previousSlot = handler.getSlot(45)
			val nextSlot = handler.getSlot(53)

			val backPressed = event.matches(TConfig.changePageKeybind) || event.matches(TConfig.previousPage)
			val nextPressed = event.matches(TConfig.changePageKeybind) || event.matches(TConfig.nextPage)

			if (backPressed && previousSlot.item.item == Items.ARROW) {
				previousSlot.clickLeftMouseButton(handler)
			} else if (nextPressed && nextSlot.item.item == Items.ARROW) {
				nextSlot.clickLeftMouseButton(handler)
			}
		}


		val slot =
			slotKeybindsWithSlot
				.find { event.matches(it.second.get()) }
				?.first ?: return

		event.cancel()

		val handler = event.screen.menu
		val invSlot = handler.getSlot(slot)

		val itemStack = invSlot.item
		val isSelected = itemStack.item == Items.LIME_DYE
		val isSelectable = itemStack.item == Items.GRAY_DYE
		if (!isSelectable && !isSelected) return
		if (!TConfig.allowUnequipping && isSelected) return

		invSlot.clickLeftMouseButton(handler)
	}

}
