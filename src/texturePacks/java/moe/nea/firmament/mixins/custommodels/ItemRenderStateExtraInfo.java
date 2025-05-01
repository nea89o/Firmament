package moe.nea.firmament.mixins.custommodels;

import moe.nea.firmament.features.texturepack.HeadModelChooser;
import net.minecraft.client.render.item.ItemRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderState.class)
public class ItemRenderStateExtraInfo implements HeadModelChooser.HasExplicitHeadModelMarker {
	boolean hasExplicitHead_firmament = false;

	@Inject(method = "clear", at = @At("HEAD"))
	private void clear(CallbackInfo ci) {
		hasExplicitHead_firmament = false;
	}

	@Override
	public void markExplicitHead_Firmament() {
		hasExplicitHead_firmament = true;
	}

	@Override
	public boolean isExplicitHeadModel_Firmament() {
		return hasExplicitHead_firmament;
	}
}
