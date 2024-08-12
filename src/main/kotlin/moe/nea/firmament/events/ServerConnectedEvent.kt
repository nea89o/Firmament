package moe.nea.firmament.events

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.ClientConnection

data class ServerConnectedEvent(
    val connection: ClientConnection
) : FirmamentEvent() {
    companion object : FirmamentEventBus<ServerConnectedEvent>() {
        init {
            ClientPlayConnectionEvents.INIT.register(ClientPlayConnectionEvents.Init { clientPlayNetworkHandler: ClientPlayNetworkHandler, minecraftClient: MinecraftClient ->
                publishSync(ServerConnectedEvent(clientPlayNetworkHandler.connection))
            })
        }
    }
}
