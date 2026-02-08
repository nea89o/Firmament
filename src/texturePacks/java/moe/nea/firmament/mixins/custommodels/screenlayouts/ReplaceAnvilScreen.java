package moe.nea.firmament.mixins.custommodels.screenlayouts;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.features.texturepack.CustomScreenLayouts;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreen.class)
public abstract class ReplaceAnvilScreen extends ItemCombinerScreen<AnvilMenu> {
	@Shadow
	private EditBox name;

	public ReplaceAnvilScreen(AnvilMenu handler, Inventory playerInventory, Component title, Identifier texture) {
		super(handler, playerInventory, title, texture);
	}

	@Inject(method = "subInit", at = @At("TAIL"))
	private void moveNameField(CallbackInfo ci) {
		var override = CustomScreenLayouts.getMover(CustomScreenLayouts.CustomScreenLayout::getNameField);
		if (override == null) return;
		int baseX = (this.width - this.imageWidth) / 2;
		int baseY = (this.height - this.imageHeight) / 2;
		name.setX(baseX + override.getX());
		name.setY(baseY + override.getY());
		if (override.getWidth() != null)
			name.setWidth(override.getWidth());
		if (override.getHeight() != null)
			name.setHeight(override.getHeight());
	}

	@WrapOperation(method = "renderLabels",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"),
		allow = 1)
	private void onDrawRepairCost(GuiGraphics instance, Font textRenderer, Component text, int x, int y, int color, Operation<Void> original) {
		var textOverride = CustomScreenLayouts.getTextMover(CustomScreenLayouts.CustomScreenLayout::getRepairCostTitle);
		original.call(instance, textRenderer,
			textOverride.replaceText(text),
			textOverride.replaceX(textRenderer, text, x),
			textOverride.replaceY(y),
			textOverride.replaceColor(text, color));
	}
}
