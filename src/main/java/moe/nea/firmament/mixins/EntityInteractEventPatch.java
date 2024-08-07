
package moe.nea.firmament.mixins;

import moe.nea.firmament.events.EntityInteractionEvent;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class EntityInteractEventPatch {
    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void onAttack(PlayerEntity player, Entity target, CallbackInfo ci) {
        EntityInteractionEvent.Companion.publish(new EntityInteractionEvent(EntityInteractionEvent.InteractionKind.ATTACK, target, Hand.MAIN_HAND));
    }

    @Inject(method = "interactEntity", at = @At("HEAD"))
    private void onInteract(PlayerEntity player, Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        EntityInteractionEvent.Companion.publish(new EntityInteractionEvent(EntityInteractionEvent.InteractionKind.INTERACT, entity, hand));
    }

    @Inject(method = "interactEntityAtLocation", at = @At("HEAD"))
    private void onInteractAtLocation(PlayerEntity player, Entity entity, EntityHitResult hitResult, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        EntityInteractionEvent.Companion.publish(new EntityInteractionEvent(EntityInteractionEvent.InteractionKind.INTERACT_AT_LOCATION, entity, hand));
    }

}
