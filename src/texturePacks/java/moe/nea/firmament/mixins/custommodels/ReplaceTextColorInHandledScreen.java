package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.features.texturepack.CustomScreenLayouts;
import moe.nea.firmament.features.texturepack.CustomTextColors;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.BeaconScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({HandledScreen.class, InventoryScreen.class, CreativeInventoryScreen.class, MerchantScreen.class,
	AnvilScreen.class, BeaconScreen.class})
public class ReplaceTextColorInHandledScreen {

	// To my future self: double check those mixins, but don't be too concerned about errors. Some of the wrapopertions
	// only apply in some of the specified subclasses.

	@WrapOperation(
		method = "drawForeground",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I"),
		expect = 0,
		require = 0)
	private int replaceTextColorWithVariableShadow(DrawContext instance, TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow, Operation<Integer> original) {
		return original.call(instance, textRenderer, text, CustomScreenLayouts.INSTANCE.mapTextToX(text, x), CustomScreenLayouts.INSTANCE.mapTextToY(text, y), CustomTextColors.INSTANCE.mapTextColor(text, color), shadow);
	}

	@WrapOperation(
		method = "drawForeground",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"),
		expect = 0,
		require = 0)
	private int replaceTextColorWithShadow(DrawContext instance, TextRenderer textRenderer, Text text, int x, int y, int color, Operation<Integer> original) {
		return original.call(instance, textRenderer, text, CustomScreenLayouts.INSTANCE.mapTextToX(text, x), CustomScreenLayouts.INSTANCE.mapTextToY(text, y), CustomTextColors.INSTANCE.mapTextColor(text, color));
	}

}
