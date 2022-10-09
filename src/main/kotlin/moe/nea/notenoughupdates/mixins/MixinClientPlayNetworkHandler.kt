package moe.nea.notenoughupdates.mixins

import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket
import moe.nea.notenoughupdates.events.WorldReadyEvent

@Mixin(ClientPlayNetworkHandler::class)
class MixinClientPlayNetworkHandler {
    @Inject(method = ["onPlayerSpawnPosition"], at = [At("RETURN")])
    fun onOnPlayerSpawnPosition(packet: PlayerSpawnPositionS2CPacket, ci: CallbackInfo) {
        WorldReadyEvent.publish(WorldReadyEvent())
    }
}
