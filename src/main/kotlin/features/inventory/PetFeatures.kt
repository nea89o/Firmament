package moe.nea.firmament.features.inventory

import net.minecraft.util.Identifier
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.petData
import moe.nea.firmament.util.render.drawGuiTexture
import moe.nea.firmament.util.useMatch

object PetFeatures : FirmamentFeature {
	override val identifier: String
		get() = "pets"

	override val config: ManagedConfig?
		get() = TConfig

	object TConfig : ManagedConfig(identifier, Category.INVENTORY) {
		val highlightEquippedPet by toggle("highlight-pet") { true }
	}

	val petMenuTitle = "Pets(?: \\([0-9]+/[0-9]+\\))?".toPattern()

	@Subscribe
	fun onSlotRender(event: SlotRenderEvents.Before) {
		if (!TConfig.highlightEquippedPet) return
		val stack = event.slot.stack
		if (stack.petData?.active == true)
			petMenuTitle.useMatch(MC.screenName ?: return) {
				event.context.drawGuiTexture(
					event.slot.x, event.slot.y, 0, 16, 16,
					Identifier.of("firmament:selected_pet_background")
				)
			}
	}


}
