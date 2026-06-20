
package moe.nea.firmament.mixins;

import moe.nea.firmament.events.EarlyResourceReloadEvent;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableResourceManager.class)
public abstract class EarlyResourceReloadPatch implements ResourceManager {
    @Inject(method = "createReload", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/SimpleReloadInstance;create(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/server/packs/resources/ReloadInstance;", shift = At.Shift.BEFORE))
    public void onResourceReload(Executor backgroundExecutor, Executor mainThreadExecutor, CompletableFuture<Unit> initialTask, List<PackResources> resourcePacks, CallbackInfoReturnable<ReloadInstance> cir) {
        EarlyResourceReloadEvent.Companion.publish(new EarlyResourceReloadEvent(this, backgroundExecutor));
    }
}
