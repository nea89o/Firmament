package moe.nea.notenoughupdates.mixins

import moe.nea.notenoughupdates.events.NEUScreenEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Suppress("CAST_NEVER_SUCCEEDS")
@Mixin(MinecraftClient::class)
class MixinMinecraft {
    @Inject(method = ["setScreen"], at = [At("HEAD")], cancellable = true)
    fun onScreenChange(screen: Screen?, ci: CallbackInfo) {
        if (NEUScreenEvents.SCREEN_OPEN.invoker().onScreenOpen((this as MinecraftClient).currentScreen, screen))
            ci.cancel()
    }
}
