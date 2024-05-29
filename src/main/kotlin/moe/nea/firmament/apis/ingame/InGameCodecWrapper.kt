/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.apis.ingame

import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

class InGameCodecWrapper(
    val wrapped: PacketCodec<PacketByteBuf, CustomPayload>,
    val direction: Direction,
) : PacketCodec<PacketByteBuf, CustomPayload> {
    enum class Direction {
        S2C,
        C2S,
        ;

        var customCodec: PacketCodec<PacketByteBuf, FirmamentCustomPayload> = createStealthyCodec()
    }

    companion object {
        fun createStealthyCodec(vararg codecs: CustomPayload.Type<PacketByteBuf, out FirmamentCustomPayload>): PacketCodec<PacketByteBuf, FirmamentCustomPayload> {
            return CustomPayload.createCodec(
                { FirmamentCustomPayload.Unhandled.createCodec(it) },
                codecs.toList()
            ) as PacketCodec<PacketByteBuf, FirmamentCustomPayload>
        }

    }

    override fun decode(buf: PacketByteBuf): CustomPayload {
        val duplicateBuffer = PacketByteBuf(buf.slice())
        val original = wrapped.decode(buf)
        val duplicate = direction.customCodec.decode(duplicateBuffer)
        if (duplicate is FirmamentCustomPayload.Unhandled)
            return original
        return JoinedCustomPayload(original, duplicate)
    }

    override fun encode(buf: PacketByteBuf, value: CustomPayload) {
        if (value is FirmamentCustomPayload) {
            direction.customCodec.encode(buf, value)
        } else {
            wrapped.encode(buf, value)
        }
    }
}
