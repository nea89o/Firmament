
package moe.nea.firmament.mixins;

import moe.nea.firmament.events.SoundReceiveEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class SoundReceiveEventPatch {
    @Inject(method = "onPlaySound", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/registry/entry/RegistryEntry;Lnet/minecraft/sound/SoundCategory;FFJ)V"), cancellable = true)
    private void postEventWhenSoundIsPlayed(PlaySoundS2CPacket packet, CallbackInfo ci) {
        var event = new SoundReceiveEvent(
            packet.getSound(),
            packet.getCategory(),
            new Vec3d(packet.getX(), packet.getY(), packet.getZ()),
            packet.getPitch(),
            packet.getVolume(),
            packet.getSeed()
        );
        SoundReceiveEvent.Companion.publish(event);
        if (event.getCancelled()) {
            ci.cancel();
        }
    }
}
