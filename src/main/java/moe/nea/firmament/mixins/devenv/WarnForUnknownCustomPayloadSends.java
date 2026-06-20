
package moe.nea.firmament.mixins.devenv;

import moe.nea.firmament.Firmament;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DiscardedPayload.class)
public class WarnForUnknownCustomPayloadSends {
    @Inject(method = "lambda$codec$0", at = @At("HEAD"))
    private static void warn(DiscardedPayload payload, FriendlyByteBuf buf, CallbackInfo ci) {
        Firmament.INSTANCE.getLogger().warn("Unknown custom payload is being sent: {}", payload);
    }
}
