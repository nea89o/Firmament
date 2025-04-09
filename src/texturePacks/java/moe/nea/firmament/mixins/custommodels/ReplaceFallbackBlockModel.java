package moe.nea.firmament.mixins.custommodels;

import moe.nea.firmament.features.texturepack.CustomBlockTextures;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BlockStateModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockModels.class)
public class ReplaceFallbackBlockModel {
    // TODO: add check to BlockDustParticle
    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private void getModel(BlockState state, CallbackInfoReturnable<BlockStateModel> cir) {
        var replacement = CustomBlockTextures.getReplacementModel(state, null);
        if (replacement != null)
            cir.setReturnValue(replacement);
    }
}
