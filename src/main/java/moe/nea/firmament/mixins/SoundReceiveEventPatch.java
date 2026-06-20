
package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import moe.nea.firmament.events.SoundReceiveEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPacketListener.class)
public class SoundReceiveEventPatch {
    @WrapWithCondition(method = "handleSoundEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;playSeededSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V"))
    private boolean postEventWhenSoundIsPlayed(ClientLevel instance, @Nullable Entity except, double x, double y, double z, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch, long seed) {
        var event = new SoundReceiveEvent(
            sound,
			source,
            new Vec3(x,y,z),
            pitch,
            volume,
            seed
        );
        SoundReceiveEvent.Companion.publish(event);
		return !event.getCancelled();
    }
}
