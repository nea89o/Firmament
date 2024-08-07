
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
        buf.skipBytes(buf.readableBytes())
        val duplicate = runCatching { direction.customCodec.decode(duplicateBuffer) }
            .getOrNull()
        if (duplicate is FirmamentCustomPayload.Unhandled || duplicate == null)
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
