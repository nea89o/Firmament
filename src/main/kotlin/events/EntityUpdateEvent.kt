package moe.nea.firmament.events

import com.mojang.datafixers.util.Pair
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.DataTracker
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.util.MC

/**
 * This event is fired when some entity properties are updated.
 * It is not fired for common changes like position, but is for less common ones,
 * like health, tracked data, names, equipment. It is always fired
 * *after* the values have been applied to the entity.
 */
sealed class EntityUpdateEvent : FirmamentEvent() {
	companion object : FirmamentEventBus<EntityUpdateEvent>() {
		@Subscribe
		fun onPlayerInventoryUpdate(event: PlayerInventoryUpdate) {
			val p = MC.player ?: return
			val updatedSlots = listOf(
				EquipmentSlot.HEAD to 39,
				EquipmentSlot.CHEST to 38,
				EquipmentSlot.LEGS to 37,
				EquipmentSlot.FEET to 36,
				EquipmentSlot.MAINHAND to p.inventory.selectedSlot, // TODO: also equipment update when you swap your selected slot perhaps
			).mapNotNull { (slot, stackIndex) ->
				val slotIndex = p.playerScreenHandler.getSlotIndex(p.inventory, stackIndex).asInt
				event.getOrNull(slotIndex)?.let {
					Pair.of(slot, it)
				}
			}
			if (updatedSlots.isNotEmpty())
				publish(EquipmentUpdate(p, updatedSlots))
		}
	}

	abstract val entity: Entity

	data class AttributeUpdate(
		override val entity: LivingEntity,
		val attributes: List<EntityAttributesS2CPacket.Entry>,
	) : EntityUpdateEvent()

	data class TrackedDataUpdate(
		override val entity: Entity,
		val trackedValues: List<DataTracker.SerializedEntry<*>>,
	) : EntityUpdateEvent()

	data class EquipmentUpdate(
		override val entity: Entity,
		val newEquipment: List<Pair<EquipmentSlot, ItemStack>>,
	) : EntityUpdateEvent()

// TODO: onEntityPassengersSet, onEntityAttach?, onEntityStatusEffect
}
