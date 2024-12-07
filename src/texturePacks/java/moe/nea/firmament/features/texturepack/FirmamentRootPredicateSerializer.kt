package moe.nea.firmament.features.texturepack

import com.google.gson.JsonObject
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import moe.nea.firmament.features.texturepack.predicates.AndPredicate

object FirmamentRootPredicateSerializer : KSerializer<FirmamentModelPredicate> {
	val delegateSerializer = kotlinx.serialization.json.JsonObject.serializer()
	override val descriptor: SerialDescriptor
		get() = SerialDescriptor("FirmamentModelRootPredicate", delegateSerializer.descriptor)

	override fun deserialize(decoder: Decoder): FirmamentModelPredicate {
		val json = decoder.decodeSerializableValue(delegateSerializer).intoGson() as JsonObject
		return AndPredicate(CustomModelOverrideParser.parsePredicates(json).toTypedArray())
	}

	override fun serialize(encoder: Encoder, value: FirmamentModelPredicate) {
		TODO("Cannot serialize firmament predicates")
	}
}
