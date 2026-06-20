package moe.nea.firmament.mixins;

import moe.nea.firmament.features.fixes.Fixes;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(EffectsInInventory.class)
public abstract class HideStatusEffectsPatch {
	@Inject(method = "canSeeEffects", at = @At("HEAD"), cancellable = true)
	private void hideStatusEffects(CallbackInfoReturnable<Boolean> cir) {
		if (Fixes.TConfig.INSTANCE.getHidePotionEffects()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
	private void conditionalRenderStatuses(GuiGraphicsExtractor graphics, Collection<MobEffectInstance> activeEffects, int x0, int yStep, int mouseX, int mouseY, int maxWidth, CallbackInfo ci) {
		if (Fixes.TConfig.INSTANCE.getHidePotionEffects()) {
			ci.cancel();
		}
	}

}
