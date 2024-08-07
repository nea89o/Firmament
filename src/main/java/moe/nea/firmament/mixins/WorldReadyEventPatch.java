

package moe.nea.firmament.mixins;

import moe.nea.firmament.events.WorldReadyEvent;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DownloadingTerrainScreen.class)
public class WorldReadyEventPatch {
    @Inject(method = "close", at = @At("HEAD"))
    public void onClose(CallbackInfo ci) {
        WorldReadyEvent.Companion.publish(new WorldReadyEvent());
    }
}
