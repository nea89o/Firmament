package moe.nea.firmament.mixins.custommodels.screenlayouts;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.features.texturepack.CustomScreenLayouts;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;

@Mixin(InventoryScreen.class)
public abstract class ReplacePlayerBackgrounds extends RecipeBookScreen<PlayerScreenHandler> {
	public ReplacePlayerBackgrounds(PlayerScreenHandler handler, RecipeBookWidget<?> recipeBook, PlayerInventory inventory, Text title) {
		super(handler, recipeBook, inventory, title);
	}


	@WrapOperation(method = "drawForeground",
		allow = 1,
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawText(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;IIIZ)I"))
	private int onDrawForegroundText(DrawContext instance, TextRenderer textRenderer, Text text, int x, int y, int color, boolean shadow, Operation<Integer> original) {
		var textOverride = CustomScreenLayouts.getTextMover(CustomScreenLayouts.CustomScreenLayout::getContainerTitle);
		return original.call(instance, textRenderer,
			textOverride.replaceText(text),
			textOverride.replaceX(textRenderer, text, x),
			textOverride.replaceY(y),
			textOverride.replaceColor(text, color),
			shadow);
	}

	@WrapWithCondition(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V"))
	private boolean onDrawBackground(DrawContext instance, Function<Identifier, RenderLayer> renderLayers, Identifier sprite, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		final var override = CustomScreenLayouts.getActiveScreenOverride();
		if (override == null || override.getBackground() == null) return true;
		override.getBackground().renderGeneric(instance, this);
		return false;
	}
	// TODO: allow moving the player
}
