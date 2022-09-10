package moe.nea.notenoughupdates.mixins

import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import moe.nea.notenoughupdates.events.ScreenOpenEvent

@Suppress("CAST_NEVER_SUCCEEDS")
@Mixin(MinecraftClient::class)
class MixinMinecraft {
    @Inject(method = ["setScreen"], at = [At("HEAD")], cancellable = true)
    fun onScreenChange(screen: Screen?, ci: CallbackInfo) {
        val event = ScreenOpenEvent((this as MinecraftClient).currentScreen, screen)
        if (ScreenOpenEvent.publish(event).cancelled)
            ci.cancel()
    }
}
