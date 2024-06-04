/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins.custompayload;

import moe.nea.firmament.apis.ingame.JoinedCustomPayload;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CustomPayloadS2CPacket.class)
public abstract class SplitJoinedCustomPayload {

    @Shadow
    public abstract CustomPayload payload();

    @Inject(method = "apply(Lnet/minecraft/network/listener/ClientCommonPacketListener;)V", at = @At("HEAD"), cancellable = true)
    private void onApply(ClientCommonPacketListener clientCommonPacketListener, CallbackInfo ci) {
        if (payload() instanceof JoinedCustomPayload joinedCustomPayload) {
            new CustomPayloadS2CPacket(joinedCustomPayload.getOriginal()).apply(clientCommonPacketListener);
            new CustomPayloadS2CPacket(joinedCustomPayload.getSmuggled()).apply(clientCommonPacketListener);
            ci.cancel();
        }
    }
}
