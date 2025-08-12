package moe.nea.firmament.mixins;

import moe.nea.firmament.features.fixes.Fixes;
import moe.nea.firmament.util.SBData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StatusEffectsDisplay.class)
public abstract class HideStatusEffectsPatch {
	@Shadow
	public abstract boolean shouldHideStatusEffectHud();

	@Inject(method = "shouldHideStatusEffectHud", at = @At("HEAD"), cancellable = true)
	private void hideStatusEffects(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(!Fixes.TConfig.INSTANCE.getHidePotionEffects() && SBData.INSTANCE.isOnSkyblock());
	}

	@Inject(method = "drawStatusEffects", at = @At("HEAD"), cancellable = true)
	private void conditionalRenderStatuses(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
		if (shouldHideStatusEffectHud() || !Fixes.TConfig.INSTANCE.getHidePotionEffects() && SBData.INSTANCE.isOnSkyblock()) {
			ci.cancel();
		}
	}

}
