package moe.nea.firmament.mixins.compat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "me.shedaniel.rei.impl.client.gui.hints.ImportantWarningsWidget")
@Pseudo
public class HideREIRecipeWarning {
	@Shadow
	private boolean visible;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void onCreateImportantWidget(CallbackInfo ci) {
		visible = false;
	}
}
