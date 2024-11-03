

package moe.nea.firmament.mixins;

import moe.nea.firmament.events.WorldReadyEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class WorldReadyEventPatch {
	@Inject(method = "joinWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setWorld(Lnet/minecraft/client/world/ClientWorld;)V", shift = At.Shift.AFTER))
	public void onClose(CallbackInfo ci) {
		WorldReadyEvent.Companion.publish(new WorldReadyEvent());
	}
}
