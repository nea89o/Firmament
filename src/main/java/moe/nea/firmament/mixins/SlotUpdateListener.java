
package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.events.PlayerInventoryUpdate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
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
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/tutorial/TutorialManager;onSlotUpdate(Lnet/minecraft/item/ItemStack;)V"))
    private void onSingleSlotUpdate(
        ScreenHandlerSlotUpdateS2CPacket packet,
        CallbackInfo ci) {
        var player = this.client.player;
        assert player != null;
        if (packet.getSyncId() == ScreenHandlerSlotUpdateS2CPacket.UPDATE_PLAYER_INVENTORY_SYNC_ID
            || packet.getSyncId() == 0) {
            PlayerInventoryUpdate.Companion.publish(new PlayerInventoryUpdate.Single(packet.getSlot(), packet.getStack()));
        } else if (packet.getSyncId() == player.currentScreenHandler.syncId) {
            // TODO: dispatch single chest slot
        }
    }

    @Inject(method = "onInventory",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
            shift = At.Shift.AFTER))
    private void onMultiSlotUpdate(InventoryS2CPacket packet, CallbackInfo ci) {
        var player = this.client.player;
        assert player != null;
        if (packet.getSyncId() == 0) {
            PlayerInventoryUpdate.Companion.publish(new PlayerInventoryUpdate.Multi(packet.getContents()));
        } else if (packet.getSyncId() == player.currentScreenHandler.syncId) {
            // TODO: dispatch multi chest
        }
    }
}
