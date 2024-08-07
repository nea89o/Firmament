
package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.BakedModelExtra;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HeadFeatureRenderer.class)
public class PatchHeadFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow
    @Final
    private HeldItemRenderer heldItemRenderer;

    @WrapOperation(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BlockItem;getBlock()Lnet/minecraft/block/Block;"))
    private Block replaceSkull(BlockItem instance, Operation<Block> original, @Local ItemStack itemStack) {
        var oldBlock = original.call(instance);
        if (oldBlock instanceof AbstractSkullBlock) {
            var bakedModel = this.heldItemRenderer.itemRenderer
                .getModel(itemStack, null, null, 0);
            if (bakedModel instanceof BakedModelExtra extra && extra.getHeadModel_firmament() != null)
                return Blocks.ENCHANTING_TABLE; // Any non skull block. Let's choose the enchanting table because it is very distinct.
        }
        return oldBlock;
    }




}
