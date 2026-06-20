
package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.events.EntityUpdateEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class EntityUpdateEventListener extends ClientCommonPacketListenerImpl {

	@Shadow
	private ClientLevel level;

	protected EntityUpdateEventListener(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
		super(client, connection, connectionState);
	}

	@Inject(method = "handleSetEquipment", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER))
	private void onEquipmentUpdate(ClientboundSetEquipmentPacket packet, CallbackInfo ci, @Local Entity entity) {
		if (entity instanceof LivingEntity livingEntity)
			EntityUpdateEvent.Companion.publish(new EntityUpdateEvent.EquipmentUpdate(livingEntity, packet.getSlots()));
	}

	@Inject(method = "handleUpdateAttributes", at = @At("TAIL"))
	private void onAttributeUpdate(ClientboundUpdateAttributesPacket packet, CallbackInfo ci) {
		EntityUpdateEvent.Companion.publish(new EntityUpdateEvent.AttributeUpdate(
			(LivingEntity) level.getEntity(packet.getEntityId()), packet.getValues()));
	}

	@Inject(method = "handleSetEntityData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/syncher/SynchedEntityData;assignValues(Ljava/util/List;)V", shift = At.Shift.AFTER))
	private void onEntityTracker(ClientboundSetEntityDataPacket packet, CallbackInfo ci, @Local(name = "entity") Entity entity) {
		EntityUpdateEvent.Companion.publish(new EntityUpdateEvent.TrackedDataUpdate(entity, packet.packedItems()));
	}
}
