/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.inventory.storageoverlay

import java.util.SortedMap
import kotlinx.serialization.serializer
import net.minecraft.client.gui.screen.Screen
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ScreenChangeEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.ScreenUtil.setScreenLater
import moe.nea.firmament.util.data.ProfileSpecificDataHolder

object StorageOverlay : FirmamentFeature {


    object Data : ProfileSpecificDataHolder<StorageData>(serializer(), "storage-data", ::StorageData)

    override val identifier: String
        get() = "storage-overlay"

    object TConfig : ManagedConfig(identifier) {
        val rows by integer("rows", 1, 5) { 3 }
        val scrollSpeed by integer("scroll-speed", 1, 50) { 10 }
        val inverseScroll by toggle("inverse-scroll") { false }
        val padding by integer("padding", 1, 20) { 5 }
        val margin by integer("margin", 1, 60) { 20 }
    }

    override val config: TConfig
        get() = TConfig

    var lastStorageOverlay: Screen? = null
    var shouldReturnToStorageOverlayFrom: Screen? = null
    var shouldReturnToStorageOverlay: Screen? = null
    var currentHandler: StorageBackingHandle? = StorageBackingHandle.None

    @Subscribe
    fun onTick(event: TickEvent) {
        rememberContent(currentHandler ?: return)
    }

    @Subscribe
    fun onScreenChangeLegacy(event: ScreenChangeEvent) {
        currentHandler = StorageBackingHandle.fromScreen(event.new)
        if (event.old is StorageOverlayScreen && !event.old.isClosing) {
            event.old.setHandler(currentHandler)
            if (currentHandler != null)
            // TODO: Consider instead only replacing rendering? might make a lot of stack handling easier
                event.cancel()
        }
    }

    @Subscribe
    fun onScreenChange(it: ScreenChangeEvent) {
        if (lastStorageOverlay != null && it.new != null) {
            shouldReturnToStorageOverlay = lastStorageOverlay
            shouldReturnToStorageOverlayFrom = it.new
            lastStorageOverlay = null
        } else if (it.old === shouldReturnToStorageOverlayFrom) {
            if (shouldReturnToStorageOverlay != null && it.new == null)
                setScreenLater(shouldReturnToStorageOverlay)
            shouldReturnToStorageOverlay = null
            shouldReturnToStorageOverlayFrom = null
        }
    }

    private fun rememberContent(handler: StorageBackingHandle) {
        // TODO: Make all of these functions work on deltas / updates instead of the entire contents
        val data = Data.data?.storageInventories ?: return
        when (handler) {
            StorageBackingHandle.None -> {}
            is StorageBackingHandle.Overview -> rememberStorageOverview(handler, data)
            is StorageBackingHandle.Page -> rememberPage(handler, data)
        }
        Data.markDirty()
    }

    private fun rememberStorageOverview(
        handler: StorageBackingHandle.Overview,
        data: SortedMap<StoragePageSlot, StorageData.StorageInventory>
    ) {
        for ((index, stack) in handler.handler.stacks.withIndex()) {
            // Ignore unloaded item stacks
            if (stack.isEmpty) continue
            val slot = StoragePageSlot.fromOverviewSlotIndex(index) ?: continue
            val isEmpty = stack.item in StorageOverlayScreen.emptyStorageSlotItems
            if (slot in data) {
                if (isEmpty)
                    data.remove(slot)
                continue
            }
            if (!isEmpty) {
                data[slot] = StorageData.StorageInventory(slot.defaultName(), slot, null)
            }
        }
    }

    private fun rememberPage(
        handler: StorageBackingHandle.Page,
        data: SortedMap<StoragePageSlot, StorageData.StorageInventory>
    ) {
        // TODO: FIXME: FIXME NOW: Definitely don't copy all of this every tick into persistence
        val newStacks =
            VirtualInventory(handler.handler.stacks.take(handler.handler.rows * 9).drop(9).map { it.copy() })
        data.compute(handler.storagePageSlot) { slot, existingInventory ->
            (existingInventory ?: StorageData.StorageInventory(
                slot.defaultName(),
                slot,
                null
            )).also {
                it.inventory = newStacks
            }
        }
    }
}
