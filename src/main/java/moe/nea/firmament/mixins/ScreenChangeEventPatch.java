

package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import moe.nea.firmament.events.ScreenChangeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class ScreenChangeEventPatch {
	@Shadow
	@Nullable
	public Screen screen;

	@Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
	public void onScreenChange(Screen screen, CallbackInfo ci, @Local(argsOnly = true) LocalRef<Screen> screenLocalRef) {
		var event = new ScreenChangeEvent(this.screen, screen);
		if (ScreenChangeEvent.Companion.publish(event).getCancelled()) {
			ci.cancel();
		} else if (event.getOverrideScreen() != null) {
			screenLocalRef.set(event.getOverrideScreen());
		}
	}
}
