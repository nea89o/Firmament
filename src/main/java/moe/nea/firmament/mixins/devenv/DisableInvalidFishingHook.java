package moe.nea.firmament.mixins.devenv;

import net.minecraft.entity.projectile.FishingBobberEntity;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FishingBobberEntity.class)
public class DisableInvalidFishingHook {
    @Redirect(method = "onSpawnPacket", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    public void onOnSpawnPacket(Logger instance, String s, Object o, Object o1) {
        // Don't warn for broken fishing hooks, since HyPixel sends a bunch of those
    }
}
