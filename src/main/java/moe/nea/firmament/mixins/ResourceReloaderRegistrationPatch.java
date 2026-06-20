
package moe.nea.firmament.mixins;

import moe.nea.firmament.events.FinalizeResourceManagerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class ResourceReloaderRegistrationPatch {
    @Shadow
    @Final
    private ReloadableResourceManager resourceManager;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;openAllSelected()Ljava/util/List;", shift = At.Shift.BEFORE))
    private void onBeforeResourcePackCreation(GameConfig gameConfig, CallbackInfo ci) {
        FinalizeResourceManagerEvent.Companion.publish(new FinalizeResourceManagerEvent(this.resourceManager));
    }
}

