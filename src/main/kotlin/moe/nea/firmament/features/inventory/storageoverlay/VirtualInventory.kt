

package moe.nea.firmament.features.inventory.storageoverlay

import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtSizeTracker

@Serializable(with = VirtualInventory.Serializer::class)
data class VirtualInventory(
    val stacks: List<ItemStack>
) {
    val rows = stacks.size / 9

    init {
        assert(stacks.size % 9 == 0)
        assert(stacks.size / 9 in 1..5)
    }


    object Serializer : KSerializer<VirtualInventory> {
        const val INVENTORY = "INVENTORY"
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("VirtualInventory", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): VirtualInventory {
            val s = decoder.decodeString()
            val n = NbtIo.readCompressed(ByteArrayInputStream(s.decodeBase64Bytes()), NbtSizeTracker.of(100_000_000))
            val items = n.getList(INVENTORY, NbtCompound.COMPOUND_TYPE.toInt())
            return VirtualInventory(items.map {
                it as NbtCompound
                if (it.isEmpty) ItemStack.EMPTY
                else runCatching {
                    ItemStack.CODEC.parse(NbtOps.INSTANCE, it).orThrow
                }.getOrElse { ItemStack.EMPTY }
            })
        }

        override fun serialize(encoder: Encoder, value: VirtualInventory) {
            val list = NbtList()
            value.stacks.forEach {
                if (it.isEmpty) list.add(NbtCompound())
                else list.add(runCatching { ItemStack.CODEC.encode(it, NbtOps.INSTANCE, NbtCompound()).orThrow }
                                  .getOrElse { NbtCompound() })
            }
            val baos = ByteArrayOutputStream()
            NbtIo.writeCompressed(NbtCompound().also { it.put(INVENTORY, list) }, baos)
            encoder.encodeString(baos.toByteArray().encodeBase64())
        }
    }
}
