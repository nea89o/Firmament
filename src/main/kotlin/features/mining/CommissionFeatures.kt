package moe.nea.firmament.features.mining

import net.minecraft.util.Identifier
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.unformattedString

object CommissionFeatures {
	object Config : ManagedConfig("commissions", Category.MINING) {
		val highlightCompletedCommissions by toggle("highlight-completed") { true }
	}


	@Subscribe
	fun onSlotRender(event: SlotRenderEvents.Before) {
		if (!Config.highlightCompletedCommissions) return
		if (MC.screenName != "Commissions") return
		val stack = event.slot.stack
		if(stack.loreAccordingToNbt.any { it.unformattedString == "COMPLETED" }) {
			event.highlight(
				MC.guiAtlasManager.getSprite(Identifier.of("firmament:completed_commission_background"))
			)
		}
	}
}
