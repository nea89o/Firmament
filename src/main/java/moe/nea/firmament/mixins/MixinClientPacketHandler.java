/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins;

import moe.nea.firmament.events.OutgoingPacketEvent;
import moe.nea.firmament.events.ParticleSpawnEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPacketHandler {
    @Inject(method = "onParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER))
    public void onParticleSpawn(ParticleS2CPacket packet, CallbackInfo ci) {
        ParticleSpawnEvent.Companion.publish(new ParticleSpawnEvent(
            packet.getParameters(),
            new Vec3d(packet.getX(), packet.getY(), packet.getZ()),
            new Vec3d(packet.getOffsetX(), packet.getOffsetY(), packet.getOffsetZ()),
            packet.isLongDistance()
        ));
    }
}
