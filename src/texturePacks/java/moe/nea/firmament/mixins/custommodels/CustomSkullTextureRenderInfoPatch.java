package moe.nea.firmament.mixins.custommodels;

import com.mojang.authlib.GameProfile;
import moe.nea.firmament.features.texturepack.CustomSkyBlockTextures;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerSkinRenderCache.RenderInfo.class)
public class CustomSkullTextureRenderInfoPatch {
	@Final
	@Shadow
	private GameProfile gameProfile;

	@Inject(
		method = "renderType",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onGetRenderType(CallbackInfoReturnable<RenderType> cir) {
		CustomSkyBlockTextures.INSTANCE.modifyRenderInfoType(this.gameProfile, cir);
	}
}
