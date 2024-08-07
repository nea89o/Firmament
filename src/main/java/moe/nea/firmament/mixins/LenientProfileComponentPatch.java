
package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.util.Uuids;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

@Mixin(ProfileComponent.class)
public class LenientProfileComponentPatch {
    // lambda in RecordCodecBuilder.create for BASE_CODEC
    @ModifyExpressionValue(method = "method_57508", at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, target = "Lnet/minecraft/util/Uuids;INT_STREAM_CODEC:Lcom/mojang/serialization/Codec;"))
    private static Codec<UUID> onStaticInit(Codec<UUID> original) {
        return Uuids.CODEC;
    }
}
