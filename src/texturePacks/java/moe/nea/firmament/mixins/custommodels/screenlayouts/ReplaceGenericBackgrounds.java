package moe.nea.firmament.mixins.custommodels.screenlayouts;

import moe.nea.firmament.features.texturepack.CustomScreenLayouts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({CraftingScreen.class, CrafterScreen.class, Generic3x3ContainerScreen.class, GenericContainerScreen.class, HopperScreen.class, ShulkerBoxScreen.class,})
public abstract class ReplaceGenericBackgrounds extends HandledScreen<ScreenHandler> {
	// TODO: split out screens with special background components like flames, arrows, etc. (maybe arrows deserve generic handling tho)
	public ReplaceGenericBackgrounds(ScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
	}

	@Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
	private void replaceDrawBackground(DrawContext context, float deltaTicks, int mouseX, int mouseY, CallbackInfo ci) {
		final var override = CustomScreenLayouts.getActiveScreenOverride();
		if (override == null || override.getBackground() == null) return;
		override.getBackground().renderGeneric(context, this);
		ci.cancel();
	}
}
