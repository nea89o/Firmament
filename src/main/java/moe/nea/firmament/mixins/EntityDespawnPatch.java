

package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.events.EntityDespawnEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class EntityDespawnPatch {
    @Inject(method = "removeEntity", at = @At(value = "TAIL"))
    private void onRemoved(int id, Entity.RemovalReason reason, CallbackInfo ci, @Local(name = "entity") @Nullable Entity entity) {
        EntityDespawnEvent.Companion.publish(new EntityDespawnEvent(entity, id, reason));
    }
}
