package moe.nea.firmament.mixins;

import moe.nea.firmament.features.fixes.Fixes;
import moe.nea.firmament.util.SBData;
import net.minecraft.client.gui.GuiGraphics;
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
	@Shadow
	public abstract boolean canSeeEffects();

	@Inject(method = "canSeeEffects", at = @At("HEAD"), cancellable = true)
	private void hideStatusEffects(CallbackInfoReturnable<Boolean> cir) {
		if (Fixes.TConfig.INSTANCE.getHidePotionEffects()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
	private void conditionalRenderStatuses(GuiGraphics guiGraphics, Collection<MobEffectInstance> collection, int i, int j, int k, int l, int m, CallbackInfo ci) {
		if (Fixes.TConfig.INSTANCE.getHidePotionEffects()) {
			ci.cancel();
		}
	}

}
