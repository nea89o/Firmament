package moe.nea.firmament.util.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializer
import net.minecraft.util.math.BlockPos

object BlockPosSerializer : KSerializer<BlockPos> {
    val delegate = serializer<List<Int>>()

    override val descriptor: SerialDescriptor
        get() = SerialDescriptor("BlockPos", delegate.descriptor)

    override fun deserialize(decoder: Decoder): BlockPos {
        val list = decoder.decodeSerializableValue(delegate)
        require(list.size == 3)
        return BlockPos(list[0], list[1], list[2])
    }

    override fun serialize(encoder: Encoder, value: BlockPos) {
        encoder.encodeSerializableValue(delegate, listOf(value.x, value.y, value.z))
    }
}
