/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.inventory.storageoverlay

import java.util.SortedMap
import kotlinx.serialization.serializer
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Items
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ScreenChangeEvent
import moe.nea.firmament.events.SlotClickEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.customgui.customGui
import moe.nea.firmament.util.data.ProfileSpecificDataHolder

object StorageOverlay : FirmamentFeature {


    object Data : ProfileSpecificDataHolder<StorageData>(serializer(), "storage-data", ::StorageData)

    override val identifier: String
        get() = "storage-overlay"

    object TConfig : ManagedConfig(identifier) {
        val alwaysReplace by toggle("always-replace") { true }
        val columns by integer("rows", 1, 10) { 3 }
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

    var lastStorageOverlay: StorageOverviewScreen? = null
    var skipNextStorageOverlayBackflip = false
    var currentHandler: StorageBackingHandle? = null

    @Subscribe
    fun onTick(event: TickEvent) {
        rememberContent(currentHandler ?: return)
    }

    @Subscribe
    fun onClick(event: SlotClickEvent) {
        if (lastStorageOverlay != null && event.slot.inventory !is PlayerInventory && event.slot.index < 9
            && event.stack.item != Items.BLACK_STAINED_GLASS_PANE
        ) {
            skipNextStorageOverlayBackflip = true
        }
    }

    @Subscribe
    fun onScreenChange(it: ScreenChangeEvent) {
        if (it.old == null && it.new == null) return
        val storageOverlayScreen = it.old as? StorageOverlayScreen
            ?: ((it.old as? HandledScreen<*>)?.customGui as? StorageOverlayCustom)?.overview
        var storageOverviewScreen = it.old as? StorageOverviewScreen
        val screen = it.new as? GenericContainerScreen
        val oldHandler = currentHandler
        currentHandler = StorageBackingHandle.fromScreen(screen)
        rememberContent(currentHandler)
        if (storageOverviewScreen != null && oldHandler is StorageBackingHandle.HasBackingScreen) {
            val player = MC.player
            assert(player != null)
            player?.networkHandler?.sendPacket(CloseHandledScreenC2SPacket(oldHandler.handler.syncId))
            if (player?.currentScreenHandler === oldHandler.handler) {
                player.currentScreenHandler = player.playerScreenHandler
            }
        }
        storageOverviewScreen = storageOverviewScreen ?: lastStorageOverlay
        if (it.new == null && storageOverlayScreen != null && !storageOverlayScreen.isExiting) {
            it.overrideScreen = storageOverlayScreen
            return
        }
        if (storageOverviewScreen != null
            && !storageOverviewScreen.isClosing
            && (currentHandler is StorageBackingHandle.Overview || currentHandler == null)
        ) {
            if (skipNextStorageOverlayBackflip) {
                skipNextStorageOverlayBackflip = false
            } else {
                it.overrideScreen = storageOverviewScreen
                lastStorageOverlay = null
            }
            return
        }
        screen ?: return
        screen.customGui = StorageOverlayCustom(
            currentHandler ?: return,
            screen,
            storageOverlayScreen ?: (if (TConfig.alwaysReplace) StorageOverlayScreen() else return))
    }

    fun rememberContent(handler: StorageBackingHandle?) {
        handler ?: return
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
