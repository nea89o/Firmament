
package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.events.EntityDespawnEvent;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class EntityDespawnPatch {
    @Inject(method = "removeEntity", at = @At(value = "TAIL"))
    private void onRemoved(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci, @Local @Nullable Entity entity) {
        EntityDespawnEvent.Companion.publish(new EntityDespawnEvent(entity, entityId, removalReason));
    }
}
