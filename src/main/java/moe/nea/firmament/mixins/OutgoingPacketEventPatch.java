

package moe.nea.firmament.mixins;

import moe.nea.firmament.events.OutgoingPacketEvent;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonNetworkHandler.class)
public class OutgoingPacketEventPatch {
    @Inject(method = "sendPacket(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (OutgoingPacketEvent.Companion.publish(new OutgoingPacketEvent(packet)).getCancelled()) {
            ci.cancel();
        }
    }
}
