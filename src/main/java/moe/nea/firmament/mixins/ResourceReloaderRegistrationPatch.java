
package moe.nea.firmament.mixins;

import moe.nea.firmament.events.FinalizeResourceManagerEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class ResourceReloaderRegistrationPatch {
    @Shadow
    @Final
    private ReloadableResourceManagerImpl resourceManager;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackManager;createResourcePacks()Ljava/util/List;", shift = At.Shift.BEFORE))
    private void onBeforeResourcePackCreation(RunArgs args, CallbackInfo ci) {
        FinalizeResourceManagerEvent.Companion.publish(new FinalizeResourceManagerEvent(this.resourceManager));
    }
}

