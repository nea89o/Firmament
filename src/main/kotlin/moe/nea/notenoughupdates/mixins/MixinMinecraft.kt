package moe.nea.notenoughupdates.mixins

import moe.nea.notenoughupdates.events.NEUScreenEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Suppress("CAST_NEVER_SUCCEEDS")
@Mixin(Minecraft::class)
class MixinMinecraft {
    @Inject(method = ["setScreen"], at = [At("HEAD")], cancellable = true)
    fun momo(screen: Screen?, ci: CallbackInfo) {
        if (NEUScreenEvents.SCREEN_OPEN.invoker().onScreenOpen((this as Minecraft).screen, screen))
            ci.cancel()
    }
}