
package moe.nea.firmament.mixins;

import moe.nea.firmament.events.ChestInventoryUpdateEvent;
import moe.nea.firmament.events.PlayerInventoryUpdate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class SlotUpdateListener extends ClientCommonNetworkHandler {
	protected SlotUpdateListener(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
		super(client, connection, connectionState);
	}

	@Inject(
		method = "onScreenHandlerSlotUpdate",
		at = @At(value = "TAIL"))
	private void onSingleSlotUpdate(
		ScreenHandlerSlotUpdateS2CPacket packet,
		CallbackInfo ci) {
		var player = this.client.player;
		assert player != null;
		if (packet.getSyncId() == 0) {
			PlayerInventoryUpdate.Companion.publish(new PlayerInventoryUpdate.Single(packet.getSlot(), packet.getStack()));
		} else if (packet.getSyncId() == player.currentScreenHandler.syncId) {
			ChestInventoryUpdateEvent.Companion.publish(
				new ChestInventoryUpdateEvent.Single(packet.getSlot(), packet.getStack())
			);
		}
	}

	@Inject(method = "onInventory",
		at = @At("TAIL"))
	private void onMultiSlotUpdate(InventoryS2CPacket packet, CallbackInfo ci) {
		var player = this.client.player;
		assert player != null;
		if (packet.syncId() == 0) {
			PlayerInventoryUpdate.Companion.publish(new PlayerInventoryUpdate.Multi(packet.contents()));
		} else if (packet.syncId() == player.currentScreenHandler.syncId) {
			ChestInventoryUpdateEvent.Companion.publish(
				new ChestInventoryUpdateEvent.Multi(packet.contents())
			);
		}
	}
}
