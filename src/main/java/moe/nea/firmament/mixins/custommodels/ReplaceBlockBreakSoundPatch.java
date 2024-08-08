package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.CustomBlockTextures;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldRenderer.class)
public class ReplaceBlockBreakSoundPatch {
// Sadly hypixel does not send a world event here and instead plays the sound on the server directly
//    @WrapOperation(method = "processWorldEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/sound/BlockSoundGroup;getBreakSound()Lnet/minecraft/sound/SoundEvent;"))
//    private SoundEvent replaceBreakSoundEvent(BlockSoundGroup instance, Operation<SoundEvent> original,
//                                              @Local(argsOnly = true) BlockPos pos, @Local BlockState blockState) {
//        var replacement = CustomBlockTextures.getReplacement(blockState, pos);
//        if (replacement != null && replacement.getSound() != null) {
//            return SoundEvent.of(replacement.getSound());
//        }
//        return original.call(instance);
//    }
}
