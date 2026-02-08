package moe.nea.firmament.mixins.custommodels.screenlayouts;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import moe.nea.firmament.features.texturepack.CustomScreenLayouts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractFurnaceScreen.class)
public abstract class ReplaceFurnaceBackgrounds<T extends AbstractFurnaceMenu> extends AbstractRecipeBookScreen<T> {
	public ReplaceFurnaceBackgrounds(T handler, RecipeBookComponent<?> recipeBook, Inventory inventory, Component title) {
		super(handler, recipeBook, inventory, title);
	}

	@WrapWithCondition(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V"), allow = 1)
	private boolean onDrawBackground(GuiGraphics instance, RenderPipeline pipeline, Identifier sprite, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		final var override = CustomScreenLayouts.getActiveScreenOverride();
		if (override == null || override.getBackground() == null) return true;
		override.getBackground().renderGeneric(instance, this);
		return false;
	}
}
