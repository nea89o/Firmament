package moe.nea.firmament.features.mining

import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.mc.accessor
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.unformattedString

object CommissionFeatures {
	@Config
	object TConfig : ManagedConfig("commissions", Category.MINING) {
		val highlightCompletedCommissions by toggle("highlight-completed") { true }
	}


	@Subscribe
	fun onSlotRender(event: SlotRenderEvents.Before) {
		if (!TConfig.highlightCompletedCommissions) return
		if (MC.screenName != "Commissions") return
		val stack = event.slot.item
		if (stack.accessor().loreAccordingToNbt.any { it.unformattedString == "COMPLETED" }) {
			event.highlight(Firmament.identifier("completed_commission_background"))
		}
	}
}
