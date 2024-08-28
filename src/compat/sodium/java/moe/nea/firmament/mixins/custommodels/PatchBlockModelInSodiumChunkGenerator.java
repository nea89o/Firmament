package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import moe.nea.firmament.features.texturepack.CustomBlockTextures;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkBuilderMeshingTask.class)
public class PatchBlockModelInSodiumChunkGenerator {
    @WrapOperation(
        method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/block/BlockModels;getModel(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/render/model/BakedModel;"))
    private BakedModel replaceBlockModel(BlockModels instance, BlockState state, Operation<BakedModel> original,
                                         @Local(name = "blockPos") BlockPos.Mutable pos) {
        var replacement = CustomBlockTextures.getReplacementModel(state, pos);
        if (replacement != null) return replacement;
        CustomBlockTextures.enterFallbackCall();
        var fallback = original.call(instance, state);
        CustomBlockTextures.exitFallbackCall();
        return fallback;
    }
}
