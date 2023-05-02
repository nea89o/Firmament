package moe.nea.notenoughupdates.mixins;

import moe.nea.notenoughupdates.events.ScreenOpenEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraft {
    @Shadow
    @Nullable
    public Screen currentScreen;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    public void onScreenChange(Screen screen, CallbackInfo ci) {
        var event = new ScreenOpenEvent(currentScreen, screen);
        if (ScreenOpenEvent.Companion.publish(event).getCancelled()) {
            ci.cancel();
        }
    }
}
