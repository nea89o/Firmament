package moe.nea.firmament.mixins.devenv;

import net.minecraft.text.TranslatableTextContent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TranslatableTextContent.class)
public abstract class EarlyInstantiateTranslations {
	@Shadow
	protected abstract void updateTranslations();

	@Inject(method = "<init>", at = @At("TAIL"))
	private void onInit(String key, String fallback, Object[] args, CallbackInfo ci) {
		updateTranslations();
	}
}
