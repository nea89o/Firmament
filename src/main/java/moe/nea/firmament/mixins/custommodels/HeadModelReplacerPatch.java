
package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.BakedModelExtra;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HeadFeatureRenderer.class)
public class HeadModelReplacerPatch<S extends LivingEntityRenderState, M extends EntityModel<S> & ModelWithHead> {
	/**
	 * This class serves to disable the replacing of head models with the vanilla block model. Vanilla first selects loads
	 * the model containing the head model regularly in {@link LivingEntityRenderer#updateRenderState}, but then discards
	 * the model in {@link HeadFeatureRenderer#render(MatrixStack, VertexConsumerProvider, int, LivingEntityRenderState, float, float)}
	 * if it detects a skull block. This serves to disable that functionality if a head model override is present.
	 */
	@WrapOperation(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/LivingEntityRenderState;FF)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BlockItem;getBlock()Lnet/minecraft/block/Block;"))
	private Block replaceSkull(BlockItem instance, Operation<Block> original, @Local BakedModel bakedModel) {
		var oldBlock = original.call(instance);
		if (oldBlock instanceof AbstractSkullBlock) {
			var extra = BakedModelExtra.cast(bakedModel);
			if (extra != null && extra.getHeadModel_firmament() != null)
				return Blocks.ENCHANTING_TABLE; // Any non skull block. Let's choose the enchanting table because it is very distinct.
		}
		return oldBlock;
	}

	/**
	 * We disable the has model override, since texture packs get precedent to server data.
	 */
	@WrapOperation(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/LivingEntityRenderState;FF)V",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;hasModel(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EquipmentSlot;)Z"))
	private boolean replaceHasModel(ItemStack stack, EquipmentSlot slot, Operation<Boolean> original,
	                                @Local BakedModel bakedModel) {
		var extra = BakedModelExtra.cast(bakedModel);
		if (extra != null && extra.getHeadModel_firmament() != null)
			return false;
		return original.call(stack, slot);
	}
}
