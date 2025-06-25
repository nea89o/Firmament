package moe.nea.firmament.features.inventory

import org.lwjgl.glfw.GLFW
import net.minecraft.item.Items
import net.minecraft.screen.slot.SlotActionType
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC

object WardrobeKeybinds : FirmamentFeature {
	override val identifier: String
		get() = "wardrobe-keybinds"

	object TConfig : ManagedConfig(identifier, Category.INVENTORY) {
		val wardrobeKeybinds by toggle("wardrobe-keybinds") { false }
		val slot1 by keyBinding("slot-1") { GLFW.GLFW_KEY_1 }
		val slot2 by keyBinding("slot-2") { GLFW.GLFW_KEY_2 }
		val slot3 by keyBinding("slot-3") { GLFW.GLFW_KEY_3 }
		val slot4 by keyBinding("slot-4") { GLFW.GLFW_KEY_4 }
		val slot5 by keyBinding("slot-5") { GLFW.GLFW_KEY_5 }
		val slot6 by keyBinding("slot-6") { GLFW.GLFW_KEY_6 }
		val slot7 by keyBinding("slot-7") { GLFW.GLFW_KEY_7 }
		val slot8 by keyBinding("slot-8") { GLFW.GLFW_KEY_8 }
		val slot9 by keyBinding("slot-9") { GLFW.GLFW_KEY_9 }
	}

	override val config: ManagedConfig?
		get() = TConfig

	@Subscribe
	fun switchSlot(it: HandledScreenKeyPressedEvent) {
		if (MC.player == null || MC.world == null || MC.interactionManager == null) return

		val regex = Regex("Wardrobe \\([12]/2\\)")
		if (!regex.matches(it.screen.title.string)) return
		if (!TConfig.wardrobeKeybinds) return

		var slot: Int? = null
		if (it.matches(TConfig.slot1)) slot = 36
		if (it.matches(TConfig.slot2)) slot = 37
		if (it.matches(TConfig.slot3)) slot = 38
		if (it.matches(TConfig.slot4)) slot = 39
		if (it.matches(TConfig.slot5)) slot = 40
		if (it.matches(TConfig.slot6)) slot = 41
		if (it.matches(TConfig.slot7)) slot = 42
		if (it.matches(TConfig.slot8)) slot = 43
		if (it.matches(TConfig.slot9)) slot = 44
		if (slot == null) return

		val itemStack = it.screen.getScreenHandler().getSlot(slot).stack
		if (itemStack.item != Items.PINK_DYE && itemStack.item != Items.LIME_DYE) return

		MC.interactionManager!!.clickSlot(it.screen.getScreenHandler().syncId, slot, GLFW.GLFW_MOUSE_BUTTON_1, SlotActionType.PICKUP, MC.player);
	}

}
