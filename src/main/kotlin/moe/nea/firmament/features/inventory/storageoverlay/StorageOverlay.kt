/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.inventory.storageoverlay

import java.util.SortedMap
import kotlinx.serialization.serializer
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ScreenChangeEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.customgui.customGui
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

    fun adjustScrollSpeed(amount: Double): Double {
        return amount * TConfig.scrollSpeed * (if (TConfig.inverseScroll) 1 else -1)
    }

    override val config: TConfig
        get() = TConfig

    var lastStorageOverlay: Screen? = null
    var currentHandler: StorageBackingHandle? = null

    @Subscribe
    fun onTick(event: TickEvent) {
        rememberContent(currentHandler ?: return)
    }

    @Subscribe
    fun onScreenChange(it: ScreenChangeEvent) {
        val storageOverlayScreen = it.old as? StorageOverlayScreen
            ?: ((it.old as? HandledScreen<*>)?.customGui as? StorageOverlayCustom)?.overview
        if (it.new == null && storageOverlayScreen != null && !storageOverlayScreen.isExiting) {
            it.overrideScreen = storageOverlayScreen
            return
        }
        val screen = it.new as? GenericContainerScreen ?: return
        currentHandler = StorageBackingHandle.fromScreen(screen)
        screen.customGui = StorageOverlayCustom(
            currentHandler as? StorageBackingHandle.Page ?: return,
            screen,
            storageOverlayScreen ?: return)
    }

    fun rememberContent(handler: StorageBackingHandle) {
        // TODO: Make all of these functions work on deltas / updates instead of the entire contents
        val data = Data.data?.storageInventories ?: return
        when (handler) {
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
            val isEmpty = stack.item in StorageOverviewScreen.emptyStorageSlotItems
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
