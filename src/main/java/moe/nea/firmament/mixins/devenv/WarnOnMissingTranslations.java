package moe.nea.firmament.mixins.devenv;

import moe.nea.firmament.features.debug.DeveloperFeatures;
import moe.nea.firmament.util.MC;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.TreeSet;

@Mixin(TranslationStorage.class)
public abstract class WarnOnMissingTranslations {
	@Shadow
	public abstract boolean hasTranslation(String key);

	@Unique
	private final Set<String> missingTranslations = new TreeSet<>();

	@Inject(method = "get", at = @At("HEAD"))
	private void onGetTranslationKey(String key, String fallback, CallbackInfoReturnable<String> cir) {
		warnForMissingTranslation(key);
	}

	@Unique
	private void warnForMissingTranslation(String key) {
		if (!key.contains("firmament")) return;
		if (hasTranslation(key)) return;
		if (!missingTranslations.add(key)) return;
		MC.INSTANCE.sendChat(Text.literal("Missing firmament translation: " + key));
		DeveloperFeatures.hookMissingTranslations(missingTranslations);
	}
}
