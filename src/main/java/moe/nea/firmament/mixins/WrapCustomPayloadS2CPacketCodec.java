/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.apis.ingame.InGameCodecWrapper;
import moe.nea.firmament.apis.ingame.JoinedCustomPayload;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(priority = 1001, value = CustomPayloadS2CPacket.class)
public abstract class WrapCustomPayloadS2CPacketCodec {

    @Shadow
    public abstract CustomPayload payload();

    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/CustomPayload;createCodec(Lnet/minecraft/network/packet/CustomPayload$CodecFactory;Ljava/util/List;)Lnet/minecraft/network/codec/PacketCodec;"))
    private static PacketCodec<PacketByteBuf, CustomPayload> wrapFactory(
        CustomPayload.CodecFactory<PacketByteBuf> unknownCodecFactory,
        List<CustomPayload.Type<PacketByteBuf, ?>> types,
        Operation<PacketCodec<PacketByteBuf, CustomPayload>> original) {

        var originalCodec = original.call(unknownCodecFactory, types);

        return new InGameCodecWrapper(originalCodec, InGameCodecWrapper.Direction.S2C);
    }


    // TODO: move to own class
    @Inject(method = "apply(Lnet/minecraft/network/listener/ClientCommonPacketListener;)V", at = @At("HEAD"), cancellable = true)
    private void onApply(ClientCommonPacketListener clientCommonPacketListener, CallbackInfo ci) {
        if (payload() instanceof JoinedCustomPayload joinedCustomPayload) {
            new CustomPayloadS2CPacket(joinedCustomPayload.getOriginal()).apply(clientCommonPacketListener);
            new CustomPayloadS2CPacket(joinedCustomPayload.getSmuggled()).apply(clientCommonPacketListener);
            ci.cancel();
        }
    }


}
