package moe.nea.firmament.mixins.custommodels.screenlayouts;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import moe.nea.firmament.features.texturepack.CustomScreenLayouts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.gui.screen.ingame.RecipeBookScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;

@Mixin(AbstractFurnaceScreen.class)
public abstract class ReplaceFurnaceBackgrounds<T extends AbstractFurnaceScreenHandler> extends RecipeBookScreen<T> {
	public ReplaceFurnaceBackgrounds(T handler, RecipeBookWidget<?> recipeBook, PlayerInventory inventory, Text title) {
		super(handler, recipeBook, inventory, title);
	}

	@WrapWithCondition(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Ljava/util/function/Function;Lnet/minecraft/util/Identifier;IIFFIIII)V"), allow = 1)
	private boolean onDrawBackground(DrawContext instance, Function<Identifier, RenderLayer> renderLayers, Identifier sprite, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
		final var override = CustomScreenLayouts.getActiveScreenOverride();
		if (override == null || override.getBackground() == null) return true;
		override.getBackground().renderGeneric(instance, this);
		return false;
	}
}
