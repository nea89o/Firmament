package moe.nea.notenoughupdates.mixins;

import moe.nea.notenoughupdates.events.WorldReadyEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {
    @Inject(method = "onPlayerSpawnPosition", at = @At("RETURN"))
    public void onOnPlayerSpawnPosition(PlayerSpawnPositionS2CPacket packet, CallbackInfo ci) {
        WorldReadyEvent.Companion.publish(new WorldReadyEvent());
    }
}
