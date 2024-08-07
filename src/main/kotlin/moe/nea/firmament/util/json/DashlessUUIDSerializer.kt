

package moe.nea.firmament.util.json

import java.util.UUID
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import moe.nea.firmament.util.parseDashlessUUID

object DashlessUUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DashlessUUIDSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        val str = decoder.decodeString()
        if ("-" in str) {
            return UUID.fromString(str)
        }
        return parseDashlessUUID(str)
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString().replace("-", ""))
    }
}
