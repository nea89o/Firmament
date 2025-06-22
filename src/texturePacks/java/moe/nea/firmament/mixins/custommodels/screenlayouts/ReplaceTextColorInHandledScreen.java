package moe.nea.firmament.mixins.custommodels.screenlayouts;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.features.texturepack.CustomScreenLayouts;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.BeaconScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.text.Text;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(HandledScreen.class)
// TODO: MerchantScreen.class, BeaconScreen.class
public class ReplaceTextColorInHandledScreen {

	@WrapOperation(
		method = "drawForeground",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I"),
		slice = @Slice(
			from = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;title:Lnet/minecraft/text/Text;", opcode = Opcodes.GETFIELD),
			to = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;playerInventoryTitle:Lnet/minecraft/text/Text;", opcode = Opcodes.GETFIELD)
		),
		allow = 1,
		require = 1)
	private int replaceContainerTitle(DrawContext instance, TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow, Operation<Integer> original) {
		var textOverride = CustomScreenLayouts.getTextMover(CustomScreenLayouts.CustomScreenLayout::getContainerTitle);
		return original.call(instance, textRenderer,
			textOverride.replaceText(text),
			textOverride.replaceX(textRenderer, text, x),
			textOverride.replaceY(y),
			textOverride.replaceColor(text, color),
			shadow);
	}

	@WrapOperation(
		method = "drawForeground",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I"),
		slice = @Slice(
			from = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;playerInventoryTitle:Lnet/minecraft/text/Text;", opcode = Opcodes.GETFIELD),
			to = @At(value = "TAIL")
		),
		allow = 1,
		require = 1)
	private int replacePlayerShadow(DrawContext instance, TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow, Operation<Integer> original) {
		var textOverride = CustomScreenLayouts.getTextMover(CustomScreenLayouts.CustomScreenLayout::getContainerTitle);
		return original.call(instance, textRenderer,
			textOverride.replaceText(text),
			textOverride.replaceX(textRenderer, text, x),
			textOverride.replaceY(y),
			textOverride.replaceColor(text, color),
			shadow);
	}
}
