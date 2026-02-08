package moe.nea.firmament.mixins.custommodels.screenlayouts;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import moe.nea.firmament.features.texturepack.CustomScreenLayouts;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractSignEditScreen.class)
public class MoveSignElements {
	@WrapWithCondition(
		method = "renderSign",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/AbstractSignEditScreen;renderSignBackground(Lnet/minecraft/client/gui/GuiGraphics;)V"))
	private boolean onDrawBackgroundSign(AbstractSignEditScreen instance, GuiGraphics drawContext) {
		final var override = CustomScreenLayouts.getActiveScreenOverride();
		if (override == null || override.getBackground() == null) return true;
		override.getBackground().renderDirect(drawContext);
		return false;
	}

	@WrapOperation(method = "renderSignText", at = {
		@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;textHighlight(IIIIZ)V")}
	)
	private void onRenderSignTextSelection(
		GuiGraphics instance, int i, int j, int k, int l, boolean bl, Operation<Void> original,
		@Local(index = 9) int messageIndex) {
		instance.pose().pushMatrix();
		final var override = CustomScreenLayouts.getSignTextMover(messageIndex);
		if (override != null) {
			instance.pose().translate(override.getX(), override.getY());
		}
		original.call(instance, i, j, k, l, bl);
		instance.pose().popMatrix();
	}
	@WrapOperation(method = "renderSignText", at = {
		@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V")}
	)
	private void onRenderSignTextFill(
            GuiGraphics instance, int x1, int y1, int x2, int y2, int color, Operation<Void> original, @Local(index = 9) int messageIndex) {
		instance.pose().pushMatrix();
		final var override = CustomScreenLayouts.getSignTextMover(messageIndex);
		if (override != null) {
			instance.pose().translate(override.getX(), override.getY());
		}
		original.call(instance, x1, y1, x2, y2, color);
		instance.pose().popMatrix();
	}

	@WrapOperation(method = "renderSignText", at = {
		@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)V")},
		expect = 2)
	private void onRenderSignTextRendering(GuiGraphics instance, Font textRenderer, String text, int x, int y, int color, boolean shadow, Operation<Void> original, @Local(index = 9) int messageIndex) {
		instance.pose().pushMatrix();
		final var override = CustomScreenLayouts.getSignTextMover(messageIndex);
		if (override != null) {
			instance.pose().translate(override.getX(), override.getY());
		}
		original.call(instance, textRenderer, text, x, y, color, shadow);
		instance.pose().popMatrix();
	}

}
