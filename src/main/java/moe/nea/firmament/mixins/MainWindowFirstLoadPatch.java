package moe.nea.firmament.mixins;

import moe.nea.firmament.Firmament;
import moe.nea.firmament.events.DebugInstantiateEvent;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MainWindowFirstLoadPatch {
	@Unique
	private static boolean hasInited = false;

	@Inject(method = "<init>(ZLnet/minecraft/client/gui/LogoDrawer;)V", at = @At("RETURN"))
	private void onCreate(boolean doBackgroundFade, LogoDrawer logoDrawer, CallbackInfo ci) {
		if (!hasInited) {
			try {
				DebugInstantiateEvent.Companion.publish(new DebugInstantiateEvent());
			} catch (Throwable t) {
				Firmament.INSTANCE.getLogger().error("Failed to instantiate debug instances", t);
				System.exit(1);
				throw t;
			}
		}
		hasInited = true;
	}
}
