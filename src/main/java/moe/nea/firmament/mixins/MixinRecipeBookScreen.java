package moe.nea.firmament.mixins;

import moe.nea.firmament.features.fixes.Fixes;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecipeBookScreen.class, priority = 999)
public class MixinRecipeBookScreen {
	@Inject(method = "addRecipeBook", at = @At("HEAD"), cancellable = true)
	public void addRecipeBook(CallbackInfo ci) {
		if (Fixes.TConfig.INSTANCE.getHideRecipeBook()) ci.cancel();
	}
}
