
package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.events.EntityUpdateEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class EntityUpdateEventListener extends ClientCommonNetworkHandler {

	@Shadow
	private ClientWorld world;

	protected EntityUpdateEventListener(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
		super(client, connection, connectionState);
	}

	@Inject(method = "onEntityEquipmentUpdate", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER))
	private void onEquipmentUpdate(EntityEquipmentUpdateS2CPacket packet, CallbackInfo ci, @Local LivingEntity entity) {
		EntityUpdateEvent.Companion.publish(new EntityUpdateEvent.EquipmentUpdate(entity, packet.getEquipmentList()));
	}

	@Inject(method = "onEntityAttributes", at = @At("TAIL"))
	private void onAttributeUpdate(EntityAttributesS2CPacket packet, CallbackInfo ci) {
		EntityUpdateEvent.Companion.publish(new EntityUpdateEvent.AttributeUpdate(
			(LivingEntity) world.getEntityById(packet.getEntityId()), packet.getEntries()));
	}

	@Inject(method = "onEntityTrackerUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;writeUpdatedEntries(Ljava/util/List;)V", shift = At.Shift.AFTER))
	private void onEntityTracker(EntityTrackerUpdateS2CPacket packet, CallbackInfo ci, @Local Entity entity) {
		EntityUpdateEvent.Companion.publish(new EntityUpdateEvent.TrackedDataUpdate(entity, packet.trackedValues()));
	}
}
