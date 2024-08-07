
package moe.nea.firmament.mixins.custompayload;

import moe.nea.firmament.apis.ingame.FirmamentCustomPayload;
import moe.nea.firmament.events.FirmamentCustomPayloadEvent;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientCommonNetworkHandler.class, priority = 500)
public class CustomPayloadEventDispatcher {
    @Inject(method = "onCustomPayload(Lnet/minecraft/network/packet/s2c/common/CustomPayloadS2CPacket;)V", at = @At("HEAD"), cancellable = true)
    private void handleFirmamentParsedPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (packet.payload() instanceof FirmamentCustomPayload customPayload) {
            FirmamentCustomPayloadEvent.Companion.publishSync(new FirmamentCustomPayloadEvent(customPayload));
            ci.cancel();
        }
    }
}
