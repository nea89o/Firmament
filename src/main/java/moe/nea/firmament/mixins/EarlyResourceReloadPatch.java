
package moe.nea.firmament.mixins;

import moe.nea.firmament.events.EarlyResourceReloadEvent;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableResourceManagerImpl.class)
public abstract class EarlyResourceReloadPatch implements ResourceManager {
    @Inject(method = "reload", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/SimpleResourceReload;start(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/resource/ResourceReload;", shift = At.Shift.BEFORE))
    public void onResourceReload(Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, List<ResourcePack> packs, CallbackInfoReturnable<ResourceReload> cir) {
        EarlyResourceReloadEvent.Companion.publish(new EarlyResourceReloadEvent(this, prepareExecutor));
    }
}
