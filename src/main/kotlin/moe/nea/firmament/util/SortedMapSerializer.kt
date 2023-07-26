package moe.nea.firmament.util

import java.util.SortedMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class SortedMapSerializer<K : Comparable<K>, V>(val keyDelegate: KSerializer<K>, val valueDelegate: KSerializer<V>) :
    KSerializer<SortedMap<K, V>> {
    val mapSerializer = MapSerializer(keyDelegate, valueDelegate)
    override val descriptor: SerialDescriptor
        get() = mapSerializer.descriptor

    override fun deserialize(decoder: Decoder): SortedMap<K, V> {
        return (mapSerializer.deserialize(decoder).toSortedMap(Comparator.naturalOrder()))
    }

    override fun serialize(encoder: Encoder, value: SortedMap<K, V>) {
        mapSerializer.serialize(encoder, value)
    }
}
