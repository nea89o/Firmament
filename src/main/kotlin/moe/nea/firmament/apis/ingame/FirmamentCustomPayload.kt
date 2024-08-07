
package moe.nea.firmament.apis.ingame

import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

interface FirmamentCustomPayload : CustomPayload {

    class Unhandled private constructor(val identifier: Identifier) : FirmamentCustomPayload {
        override fun getId(): CustomPayload.Id<out CustomPayload> {
            return CustomPayload.id(identifier.toString())
        }

        companion object {
            fun <B : ByteBuf> createCodec(identifier: Identifier): PacketCodec<B, Unhandled> {
                return object : PacketCodec<B, Unhandled> {
                    override fun decode(buf: B): Unhandled {
                        return Unhandled(identifier)
                    }

                    override fun encode(buf: B, value: Unhandled) {
                        // we will never send an unhandled packet stealthy
                    }
                }
            }
        }

    }
}
