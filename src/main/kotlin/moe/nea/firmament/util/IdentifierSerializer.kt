
package moe.nea.firmament.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.util.Identifier

object IdentifierSerializer : KSerializer<Identifier> {
    val delegateSerializer = String.serializer()
    override val descriptor: SerialDescriptor
        get() = SerialDescriptor("Identifier", delegateSerializer.descriptor)

    override fun deserialize(decoder: Decoder): Identifier {
        return Identifier.of(decoder.decodeSerializableValue(delegateSerializer))
    }

    override fun serialize(encoder: Encoder, value: Identifier) {
        encoder.encodeSerializableValue(delegateSerializer, value.toString())
    }
}
