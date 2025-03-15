package moe.nea.firmament.events

import com.mojang.datafixers.util.Pair
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.DataTracker
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket

/**
 * This event is fired when some entity properties are updated.
 * It is not fired for common changes like position, but is for less common ones,
 * like health, tracked data, names, equipment. It is always fired
 * *after* the values have been applied to the entity.
 */
sealed class EntityUpdateEvent : FirmamentEvent() {
	companion object : FirmamentEventBus<EntityUpdateEvent>()

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
