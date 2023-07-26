@file:UseSerializers(SortedMapSerializer::class)
package moe.nea.firmament.features.inventory.storageoverlay

import java.util.SortedMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import moe.nea.firmament.util.SortedMapSerializer

@Serializable
data class StorageData(
    val storageInventories: SortedMap<StoragePageSlot, StorageInventory> = sortedMapOf()
) {
    @Serializable
    data class StorageInventory(
        var title: String,
        val slot: StoragePageSlot,
        var inventory: VirtualInventory?,
    )
}
