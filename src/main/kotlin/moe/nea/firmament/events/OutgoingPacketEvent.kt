/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import net.minecraft.network.packet.Packet

data class OutgoingPacketEvent(val packet: Packet<*>) : FirmamentEvent.Cancellable() {
    companion object : FirmamentEventBus<OutgoingPacketEvent>()
}
