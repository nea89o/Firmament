package moe.nea.firmament.mixins.custommodels.screenlayouts;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.features.texturepack.CustomScreenLayouts;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.ForgingScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreen.class)
public abstract class ReplaceAnvilScreen extends ForgingScreen<AnvilScreenHandler> {
	@Shadow
	private TextFieldWidget nameField;

	public ReplaceAnvilScreen(AnvilScreenHandler handler, PlayerInventory playerInventory, Text title, Identifier texture) {
		super(handler, playerInventory, title, texture);
	}

	@Inject(method = "setup", at = @At("TAIL"))
	private void moveNameField(CallbackInfo ci) {
		var override = CustomScreenLayouts.getMover(CustomScreenLayouts.CustomScreenLayout::getNameField);
		if (override == null) return;
		int baseX = (this.width - this.backgroundWidth) / 2;
		int baseY = (this.height - this.backgroundHeight) / 2;
		nameField.setX(baseX + override.getX());
		nameField.setY(baseY + override.getY());
		if (override.getWidth() != null)
			nameField.setWidth(override.getWidth());
		if (override.getHeight() != null)
			nameField.setHeight(override.getHeight());
	}

	@WrapOperation(method = "drawForeground",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)I"),
		allow = 1)
	private int onDrawRepairCost(DrawContext instance, TextRenderer textRenderer, Text text, int x, int y, int color, Operation<Integer> original) {
		var textOverride = CustomScreenLayouts.getTextMover(CustomScreenLayouts.CustomScreenLayout::getRepairCostTitle);
		return original.call(instance, textRenderer,
			textOverride.replaceText(text),
			textOverride.replaceX(textRenderer, text, x),
			textOverride.replaceY(y),
			textOverride.replaceColor(text, color));
	}
}
