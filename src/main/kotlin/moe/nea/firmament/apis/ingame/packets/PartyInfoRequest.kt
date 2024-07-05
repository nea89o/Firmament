/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.apis.ingame.packets

import io.netty.buffer.ByteBuf
import java.util.UUID
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier
import net.minecraft.util.Uuids
import moe.nea.firmament.apis.ingame.FirmamentCustomPayload

interface FirmamentCustomPayloadMeta<T : FirmamentCustomPayload> {
    val ID: CustomPayload.Id<T>
    val CODEC: PacketCodec<PacketByteBuf, T>

    fun id(name: String): CustomPayload.Id<T> {
        return CustomPayload.Id<T>(Identifier.of(name))
    }

    fun intoType(): CustomPayload.Type<PacketByteBuf, T> {
        return CustomPayload.Type(ID, CODEC)
    }
}

data class PartyInfoRequest(val version: Int) : FirmamentCustomPayload {
    companion object : FirmamentCustomPayloadMeta<PartyInfoRequest> {
        override val ID = id("hypixel:party_info")
        override val CODEC =
            PacketCodecs.VAR_INT.cast<PacketByteBuf>()
                .xmap(::PartyInfoRequest, PartyInfoRequest::version)
    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ID
    }
}

sealed interface PartyInfoResponseV
sealed interface HypixelVersionedPacketData<out T>
data class HypixelSuccessfulResponse<T>(val data: T) : HypixelVersionedPacketData<T>
data class HypixelUnknownVersion(val version: Int) : HypixelVersionedPacketData<Nothing>
data class HypixelApiError(val label: String, val errorId: Int) : HypixelVersionedPacketData<Nothing> {
    companion object {
        fun <B : ByteBuf> createCodec(label: String): PacketCodec<B, HypixelApiError> {
            return PacketCodecs.VAR_INT
                .cast<B>()
                .xmap({ HypixelApiError(label, it) }, HypixelApiError::errorId)
        }
    }
}

object CodecUtils {
    fun <B : PacketByteBuf, T> dispatchVersioned(
        versions: Map<Int, PacketCodec<B, out T>>,
        errorCodec: PacketCodec<B, HypixelApiError>
    ): PacketCodec<B, HypixelVersionedPacketData<T>> {
        return object : PacketCodec<B, HypixelVersionedPacketData<T>> {
            override fun decode(buf: B): HypixelVersionedPacketData<T> {
                if (!buf.readBoolean()) {
                    return errorCodec.decode(buf)
                }
                val version = buf.readVarInt()
                val versionCodec = versions[version]
                    ?: return HypixelUnknownVersion(version)
                return HypixelSuccessfulResponse(versionCodec.decode(buf))
            }

            override fun encode(buf: B, value: HypixelVersionedPacketData<T>?) {
                error("Cannot encode a hypixel packet")
            }
        }
    }

    fun <B : PacketByteBuf, T> dispatchS2CBoolean(
        ifTrue: PacketCodec<B, out T>,
        ifFalse: PacketCodec<B, out T>
    ): PacketCodec<B, T> {
        return object : PacketCodec<B, T> {
            override fun decode(buf: B): T {
                return if (buf.readBoolean()) {
                    ifTrue.decode(buf)
                } else {
                    ifFalse.decode(buf)
                }
            }

            override fun encode(buf: B, value: T) {
                error("Cannot reverse dispatch boolean")
            }
        }
    }

}


data object PartyInfoResponseVUnknown : PartyInfoResponseV
data class PartyInfoResponseV1(
    val leader: UUID?,
    val members: Set<UUID>,
) : PartyInfoResponseV {
    data object PartyMember
    companion object {
        val CODEC: PacketCodec<PacketByteBuf, PartyInfoResponseV1> =
            CodecUtils.dispatchS2CBoolean(
                PacketCodec.tuple(
                    Uuids.PACKET_CODEC, PartyInfoResponseV1::leader,
                    Uuids.PACKET_CODEC.collect(PacketCodecs.toCollection(::HashSet)), PartyInfoResponseV1::members,
                    ::PartyInfoResponseV1
                ),
                PacketCodec.unit(PartyInfoResponseV1(null, setOf())))
    }
}


data class PartyInfoResponse(val data: HypixelVersionedPacketData<PartyInfoResponseV>) : FirmamentCustomPayload {
    companion object : FirmamentCustomPayloadMeta<PartyInfoResponse> {
        override val ID = id("hypixel:party_info")
        override val CODEC =
            CodecUtils
                .dispatchVersioned<PacketByteBuf, PartyInfoResponseV>(
                    mapOf(
                        1 to PartyInfoResponseV1.CODEC,
                    ),
                    HypixelApiError.createCodec("PartyInfoResponse"))
                .xmap(::PartyInfoResponse, PartyInfoResponse::data)

    }

    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ID
    }
}
