package moe.nea.firmament.mixins.custommodels;

import moe.nea.firmament.features.texturepack.CustomSkyBlockTextures;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.SkullBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CustomHeadLayer.class)
public class CustomSkullTextureHeadPatch {
	@Inject(
		method = "resolveSkullRenderType",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onResolveWornSkullRenderType(LivingEntityRenderState renderState, SkullBlock.Type type, CallbackInfoReturnable<RenderType> cir) {
		if (renderState.wornHeadProfile != null) {
			CustomSkyBlockTextures.INSTANCE.modifySkullTexture(type, renderState.wornHeadProfile, cir);
		}
	}
}
