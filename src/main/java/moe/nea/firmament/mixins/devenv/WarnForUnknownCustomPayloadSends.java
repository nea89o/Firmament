
package moe.nea.firmament.mixins.devenv;

import moe.nea.firmament.Firmament;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.UnknownCustomPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(UnknownCustomPayload.class)
public class WarnForUnknownCustomPayloadSends {
    @Inject(method = "method_56493", at = @At("HEAD"))
    private static void warn(UnknownCustomPayload value, PacketByteBuf buf, CallbackInfo ci) {
        Firmament.INSTANCE.getLogger().warn("Unknown custom payload is being sent: {}", value);
    }
}
