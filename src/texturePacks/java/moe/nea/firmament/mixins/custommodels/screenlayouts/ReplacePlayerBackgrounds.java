package moe.nea.firmament.mixins.custommodels.screenlayouts;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import moe.nea.firmament.features.texturepack.CustomScreenLayouts;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InventoryScreen.class)
public abstract class ReplacePlayerBackgrounds extends AbstractRecipeBookScreen<InventoryMenu> {
	public ReplacePlayerBackgrounds(InventoryMenu handler, RecipeBookComponent<?> recipeBook, Inventory inventory, Component title) {
		super(handler, recipeBook, inventory, title);
	}


	@WrapOperation(method = "renderLabels",
		allow = 1,
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V"))
	private void onDrawForegroundText(GuiGraphics instance, Font textRenderer, Component text, int x, int y, int color, boolean shadow, Operation<Void> original) {
		var textOverride = CustomScreenLayouts.getTextMover(CustomScreenLayouts.CustomScreenLayout::getContainerTitle);
		original.call(instance, textRenderer,
			textOverride.replaceText(text),
			textOverride.replaceX(textRenderer, text, x),
			textOverride.replaceY(y),
			textOverride.replaceColor(text, color),
			shadow);
	}

	@WrapWithCondition(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V"))
	private boolean onDrawBackground(GuiGraphics instance, RenderPipeline pipeline, Identifier sprite, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		final var override = CustomScreenLayouts.getActiveScreenOverride();
		if (override == null || override.getBackground() == null) return true;
		override.getBackground().renderGeneric(instance, this);
		return false;
	}
	// TODO: allow moving the player
}
