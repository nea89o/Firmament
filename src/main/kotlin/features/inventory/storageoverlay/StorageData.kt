

@file:UseSerializers(SortedMapSerializer::class)
package moe.nea.firmament.features.inventory.storageoverlay

import java.util.SortedMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import moe.nea.firmament.util.SortedMapSerializer

@Serializable
data class StorageData(
    val storageInventories: SortedMap<StoragePageSlot, StorageInventory> = sortedMapOf(),
    val customNames: SortedMap<StoragePageSlot, String> = sortedMapOf(),
) {
    /** The name to show for [slot]: the user assigned name if there is one, otherwise the vanilla default. */
    fun displayName(slot: StoragePageSlot): String = customNames[slot] ?: slot.defaultName()

    @Serializable
    data class StorageInventory(
        val slot: StoragePageSlot,
        var inventory: VirtualInventory?,
    )
}
