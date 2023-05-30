package moe.nea.firmament.mixins.devenv;

import moe.nea.firmament.features.debug.DeveloperFeatures;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;

@Mixin(Keyboard.class)
public class MixinKeyboard {
    @Redirect(method = "processF3", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;reloadResources()Ljava/util/concurrent/CompletableFuture;"))
    public CompletableFuture<Void> redirectReloadResources(MinecraftClient instance) {
        return DeveloperFeatures.hookOnBeforeResourceReload(instance);
    }
}
