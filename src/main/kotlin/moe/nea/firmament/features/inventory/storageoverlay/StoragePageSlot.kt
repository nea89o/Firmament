package moe.nea.firmament.features.inventory.storageoverlay

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import moe.nea.firmament.util.MC

@Serializable(with = StoragePageSlot.Serializer::class)
data class StoragePageSlot(val index: Int) : Comparable<StoragePageSlot> {
    object Serializer : KSerializer<StoragePageSlot> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("StoragePageSlot", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): StoragePageSlot {
            return StoragePageSlot(decoder.decodeInt())
        }

        override fun serialize(encoder: Encoder, value: StoragePageSlot) {
            encoder.encodeInt(value.index)
        }
    }

    init {
        assert(index in 0 until (3 * 9))
    }

    val isEnderChest get() = index < 9
    val isBackPack get() = !isEnderChest
    val slotIndexInOverviewPage get() = if (isEnderChest) index + 9 else index + 18
    fun defaultName(): String = if (isEnderChest) "Ender Chest #${index + 1}" else "Backpack #${index - 9 + 1}"

    fun navigateTo() {
        if (isBackPack) {
            MC.sendCommand("backpack ${index - 9 + 1}")
        } else {
            MC.sendCommand("enderchest ${index + 1}")
        }
    }

    companion object {
        fun fromOverviewSlotIndex(slot: Int): StoragePageSlot? {
            if (slot in 9 until 18) return StoragePageSlot(slot - 9)
            if (slot in 27 until 45) return StoragePageSlot(slot - 27 + 9)
            return null
        }

        fun ofEnderChestPage(slot: Int): StoragePageSlot {
            assert(slot in 1..9)
            return StoragePageSlot(slot - 1)
        }

        fun ofBackPackPage(slot: Int): StoragePageSlot {
            assert(slot in 1..18)
            return StoragePageSlot(slot - 1 + 9)
        }
    }

    override fun compareTo(other: StoragePageSlot): Int {
        return this.index - other.index
    }
}
