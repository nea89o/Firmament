package moe.nea.firmament.mixins.feature;

import moe.nea.firmament.features.fixes.Fixes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class DisableSlotHighlights {
	@Shadow
	public abstract ItemStack getStack();

	@Inject(method = "canBeHighlighted", at = @At("HEAD"), cancellable = true)
	private void dontHighlight(CallbackInfoReturnable<Boolean> cir) {
		if (!Fixes.TConfig.INSTANCE.getHideSlotHighlights()) return;
		var display = getStack().get(DataComponentTypes.TOOLTIP_DISPLAY);
		if (display != null && display.hideTooltip())
			cir.setReturnValue(false);
	}
}
