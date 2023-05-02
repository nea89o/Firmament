package moe.nea.notenoughupdates.mixins;

import moe.nea.notenoughupdates.events.WorldReadyEvent;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DownloadingTerrainScreen.class)
public class MixinDownloadingTerrainScreen {
    @Inject(method = "close", at = @At("HEAD"))
    public void onClose(CallbackInfo ci) {
        WorldReadyEvent.Companion.publish(new WorldReadyEvent());
    }
}
