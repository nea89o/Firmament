

package moe.nea.firmament.mixins.custommodels;

import moe.nea.firmament.features.texturepack.CustomSkyBlockTextures;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkullBlockEntityRenderer.class)
public class CustomSkullTexturePatch {
	@Inject(
		method = "getRenderLayer(Lnet/minecraft/block/SkullBlock$SkullType;Lnet/minecraft/component/type/ProfileComponent;Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void onGetRenderLayer(SkullBlock.SkullType type, ProfileComponent profile, Identifier texture, CallbackInfoReturnable<RenderLayer> cir) {
		CustomSkyBlockTextures.INSTANCE.modifySkullTexture(type, profile, cir);
	}
}
