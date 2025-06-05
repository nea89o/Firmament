package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.CustomBlockTextures;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerInteractionManager.class)
public class ReplaceBlockHitSoundPatch {
    @WrapOperation(method = "updateBlockBreakingProgress",
		at = @At(value = "NEW", target = "(Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFLnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/client/sound/PositionedSoundInstance;"))
    private PositionedSoundInstance replaceSound(
        SoundEvent sound, SoundCategory category, float volume, float pitch,
        Random random, BlockPos pos, Operation<PositionedSoundInstance> original,
        @Local BlockState blockState) {
        var replacement = CustomBlockTextures.getReplacement(blockState, pos);
        if (replacement != null && replacement.getSound() != null) {
            sound = SoundEvent.of(replacement.getSound());
        }
        return original.call(sound, category, volume, pitch, random, pos);
    }
}
