

package moe.nea.firmament.mixins.custommodels;

import moe.nea.firmament.features.texturepack.CustomSkyBlockTextures;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkullBlockRenderer.class)
public class CustomSkullTexturePatch {
	@Inject(
		method = "resolveSkullRenderType",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onGetRenderLayer(SkullBlock.Type skullType, SkullBlockEntity blockEntity, CallbackInfoReturnable<RenderType> cir) {
		CustomSkyBlockTextures.INSTANCE.modifySkullTexture(skullType, blockEntity.getOwnerProfile(), cir);
	}
}
