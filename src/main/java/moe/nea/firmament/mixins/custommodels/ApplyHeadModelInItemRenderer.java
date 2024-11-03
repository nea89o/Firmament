
package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import moe.nea.firmament.features.texturepack.BakedModelExtra;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ApplyHeadModelInItemRenderer {
	@Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;ZF)V",
		at = @At("HEAD"))
	private void applyHeadModel(ItemStack stack, ModelTransformationMode transformationMode, boolean leftHanded,
	                            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay,
	                            BakedModel model, boolean useInventoryModel, float z, CallbackInfo ci,
	                            @Local(argsOnly = true) LocalRef<BakedModel> modelMut
	) {
		var extra = BakedModelExtra.cast(model);
		if (transformationMode == ModelTransformationMode.HEAD && extra != null) {
			var headModel = extra.getHeadModel_firmament();
			if (headModel != null) {
				modelMut.set(headModel);
			}
		}
	}
}
