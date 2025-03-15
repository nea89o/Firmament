package moe.nea.firmament.features.debug

import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.Entity
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.commands.thenLiteral
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.EntityUpdateEvent
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.tr

object AnimatedClothingScanner {

	var observedEntity: Entity? = null

	@OptIn(ExperimentalStdlibApi::class)
	@Subscribe
	fun onUpdate(event: EntityUpdateEvent) {
		if (event.entity != observedEntity) return
		if (event is EntityUpdateEvent.EquipmentUpdate) {
			event.newEquipment.forEach {
				val id = it.second.skyBlockId?.neuItem
				val colour = it.second.get(DataComponentTypes.DYED_COLOR)
					?.rgb?.toHexString(HexFormat.UpperCase)
					?.let { " #$it" } ?: ""
				MC.sendChat(tr("firmament.fitstealer.update",
				               "[FIT CHECK][${MC.currentTick}] ${it.first.asString()} => ${id}${colour}"))
			}
		}
	}

	@Subscribe
	fun onSubCommand(event: CommandEvent.SubCommand) {
		event.subcommand("dev") {
			thenLiteral("stealthisfit") {
				thenExecute {
					observedEntity =
						if (observedEntity == null) MC.instance.targetedEntity else null

					MC.sendChat(
						observedEntity?.let {
							tr("firmament.fitstealer.targeted", "Observing the equipment of ${it.name}.")
						} ?: tr("firmament.fitstealer.targetlost", "No longer logging equipment."),
					)
				}
			}
		}
	}
}
