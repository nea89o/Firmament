package moe.nea.firmament.mixins.custommodels.screenlayouts;

import moe.nea.firmament.features.texturepack.CustomScreenLayouts;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({HandledScreen.class, RecipeBookScreen.class})
public class ExpandScreenBoundaries {
	@Inject(method = "isClickOutsideBounds", at = @At("HEAD"), cancellable = true)
	private void onClickOutsideBounds(double mouseX, double mouseY, int left, int top, int button, CallbackInfoReturnable<Boolean> cir) {
		var background = CustomScreenLayouts.getMover(CustomScreenLayouts.CustomScreenLayout::getBackground);
		if (background == null) return;
		var x = background.getX() + left;
		var y = background.getY() + top;
		cir.setReturnValue(mouseX < (double) x || mouseY < (double) y || mouseX >= (double) (x + background.getWidth()) || mouseY >= (double) (y + background.getHeight()));
	}
}
