package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.CustomBlockTextures;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockRenderManager.class)
public class ReplaceBlockRenderManagerBlockModel {
    @WrapOperation(method = "renderBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockRenderManager;getModel(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/render/model/BakedModel;"))
    private BakedModel replaceModelInRenderBlock(
        BlockRenderManager instance, BlockState state, Operation<BakedModel> original, @Local(argsOnly = true) BlockPos pos) {
        var replacement = CustomBlockTextures.getReplacementModel(state, pos);
        if (replacement != null) return replacement;
        CustomBlockTextures.enterFallbackCall();
        var fallback = original.call(instance, state);
        CustomBlockTextures.exitFallbackCall();
        return fallback;
    }

    @WrapOperation(method = "renderDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockModels;getModel(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/render/model/BakedModel;"))
    private BakedModel replaceModelInRenderDamage(
        BlockModels instance, BlockState state, Operation<BakedModel> original, @Local(argsOnly = true) BlockPos pos) {
        var replacement = CustomBlockTextures.getReplacementModel(state, pos);
        if (replacement != null) return replacement;
        CustomBlockTextures.enterFallbackCall();
        var fallback = original.call(instance, state);
        CustomBlockTextures.exitFallbackCall();
        return fallback;
    }
}
