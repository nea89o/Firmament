

package moe.nea.firmament.events

import net.minecraft.network.packet.Packet

data class OutgoingPacketEvent(val packet: Packet<*>) : FirmamentEvent.Cancellable() {
    companion object : FirmamentEventBus<OutgoingPacketEvent>()
}
