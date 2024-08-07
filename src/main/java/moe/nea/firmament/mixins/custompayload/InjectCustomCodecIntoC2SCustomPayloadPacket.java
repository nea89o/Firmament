
package moe.nea.firmament.mixins.custompayload;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.apis.ingame.InGameCodecWrapper;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(priority = 1001, value = CustomPayloadC2SPacket.class)
public class InjectCustomCodecIntoC2SCustomPayloadPacket {

    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/CustomPayload;createCodec(Lnet/minecraft/network/packet/CustomPayload$CodecFactory;Ljava/util/List;)Lnet/minecraft/network/codec/PacketCodec;"))
    private static PacketCodec<PacketByteBuf, CustomPayload> wrapFactory(
        CustomPayload.CodecFactory<PacketByteBuf> unknownCodecFactory,
        List<CustomPayload.Type<PacketByteBuf, ?>> types,
        Operation<PacketCodec<PacketByteBuf, CustomPayload>> original) {

        var originalCodec = original.call(unknownCodecFactory, types);

        return new InGameCodecWrapper(originalCodec, InGameCodecWrapper.Direction.C2S);
    }
}
