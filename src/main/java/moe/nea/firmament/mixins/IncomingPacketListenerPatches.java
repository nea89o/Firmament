

package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.brigadier.CommandDispatcher;
import moe.nea.firmament.events.MaskCommands;
import moe.nea.firmament.events.ParticleSpawnEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class IncomingPacketListenerPatches {


    @ModifyExpressionValue(method = "onCommandTree", at = @At(value = "NEW", target = "(Lcom/mojang/brigadier/tree/RootCommandNode;)Lcom/mojang/brigadier/CommandDispatcher;", remap = false))
    public CommandDispatcher onOnCommandTree(CommandDispatcher dispatcher) {
        MaskCommands.Companion.publish(new MaskCommands(dispatcher));
        return dispatcher;
    }

    @Inject(method = "onParticle", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), cancellable = true)
    public void onParticleSpawn(ParticleS2CPacket packet, CallbackInfo ci) {
        var event = new ParticleSpawnEvent(
            packet.getParameters(),
            new Vec3d(packet.getX(), packet.getY(), packet.getZ()),
            new Vector3f(packet.getOffsetX(), packet.getOffsetY(), packet.getOffsetZ()),
            packet.isLongDistance(),
            packet.getCount(),
            packet.getSpeed()
        );
        ParticleSpawnEvent.Companion.publish(event);
        if (event.getCancelled())
            ci.cancel();
    }
}
