package moe.nea.firmament.mixins.custommodels;

import moe.nea.firmament.features.texturepack.BakedModelExtra;
import moe.nea.firmament.features.texturepack.TintOverrides;
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

@Mixin(value = ItemRenderer.class, priority = 1010)
public class ItemRendererTintContextPatch {
	@Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;ZF)V",
		at = @At(value = "HEAD"), allow = 1)
	private void onStartRendering(ItemStack stack, ModelTransformationMode transformationMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, boolean useInventoryModel, float z, CallbackInfo ci) {
		var extra = BakedModelExtra.cast(model);
		if (extra != null) {
			TintOverrides.Companion.enter(extra.getTintOverrides_firmament());
		}
	}

	@Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;ZLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IILnet/minecraft/client/render/model/BakedModel;ZF)V",
		at = @At("TAIL"), allow = 1)
	private void onEndRendering(ItemStack stack, ModelTransformationMode transformationMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model, boolean useInventoryModel, float z, CallbackInfo ci) {
		var extra = BakedModelExtra.cast(model);
		if (extra != null) {
			TintOverrides.Companion.exit(extra.getTintOverrides_firmament());
		}
	}
}
