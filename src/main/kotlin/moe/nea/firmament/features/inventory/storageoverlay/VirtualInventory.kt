/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.inventory.storageoverlay

import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
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
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import net.minecraft.nbt.NbtSizeTracker
import moe.nea.firmament.util.MC

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
            return VirtualInventory(items.map { ItemStack.fromNbtOrEmpty(MC.defaultRegistries, it as NbtCompound) })
        }

        override fun serialize(encoder: Encoder, value: VirtualInventory) {
            val list = NbtList()
            value.stacks.forEach {
                list.add(it.encode(MC.defaultRegistries))
            }
            val baos = ByteArrayOutputStream()
            NbtIo.writeCompressed(NbtCompound().also { it.put(INVENTORY, list) }, baos)
            encoder.encodeString(baos.toByteArray().encodeBase64())
        }
    }
}
