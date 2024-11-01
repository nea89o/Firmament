package moe.nea.firmament.mixins.custommodels;

import moe.nea.firmament.features.texturepack.TintOverrides;
import moe.nea.firmament.init.ItemColorsSodiumRiser;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemColors.class)
public class ItemColorRemovalPatch {

	/**
	 * @see ItemColorsSodiumRiser
	 */
	private @Nullable ItemColorProvider overrideSodium_firmament(@Nullable ItemColorProvider original) {
		var tintOverrides = TintOverrides.Companion.getCurrentOverrides();
		if (!tintOverrides.hasOverrides()) return original;
		return (stack, tintIndex) -> {
			var override = tintOverrides.getOverride(tintIndex);
			if (override != null) return override;
			if (original != null) return original.getColor(stack, tintIndex);
			return -1;
		};
	}


	@Inject(method = "getColor", at = @At("HEAD"), cancellable = true)
	private void overrideGetColorCall(ItemStack item, int tintIndex, CallbackInfoReturnable<Integer> cir) {
		var tintOverrides = TintOverrides.Companion.getCurrentOverrides();
		var override = tintOverrides.getOverride(tintIndex);
		if (override != null)
			cir.setReturnValue(override);
	}
}
