package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import moe.nea.firmament.features.fixes.Fixes;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class HideStatusEffectsPatch {
	@Shadow
	public abstract boolean shouldHideStatusEffectHud();

	@Inject(method = "shouldHideStatusEffectHud", at = @At("HEAD"), cancellable = true)
	private void hideStatusEffects(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(!Fixes.TConfig.INSTANCE.getHidePotionEffects());
	}

	@WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/StatusEffectsDisplay;drawStatusEffects(Lnet/minecraft/client/gui/DrawContext;IIF)V"))
	private boolean conditionalRenderStatuses(StatusEffectsDisplay instance, DrawContext context, int mouseX, int mouseY, float tickDelta) {
		return shouldHideStatusEffectHud() || !Fixes.TConfig.INSTANCE.getHidePotionEffects();
	}

}
