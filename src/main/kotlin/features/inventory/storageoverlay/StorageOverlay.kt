package moe.nea.firmament.features.inventory.storageoverlay

import io.github.notenoughupdates.moulconfig.ChromaColour
import java.util.SortedMap
import kotlinx.serialization.serializer
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.Items
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket
import net.minecraft.network.chat.Component
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ChestInventoryUpdateEvent
import moe.nea.firmament.events.ScreenChangeEvent
import moe.nea.firmament.events.SlotClickEvent
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.async.discard
import moe.nea.firmament.util.customgui.customGui
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.data.ProfileSpecificDataHolder

object StorageOverlay {

	@Config
	object Data : ProfileSpecificDataHolder<StorageData>(serializer(), "storage-data", ::StorageData)

	val identifier: String
		get() = "storage-overlay"

	@Config
	object TConfig : ManagedConfig(identifier, Category.INVENTORY) {
		val alwaysReplace by toggle("always-replace") { true }
		val outlineActiveStoragePage by toggle("outline-active-page") { false }
		val outlineActiveStoragePageColour by colour("outline-active-page-colour") {
			ChromaColour.fromRGB(
				255,
				255,
				0,
				0,
				255
			)
		}
		val showInactivePageTooltips by toggle("inactive-page-tooltips") { false }
		val columns by integer("rows", 1, 10) { 3 }
		val height by integer("height", 80, 3000) { 3 * 18 * 6 }
		val retainScroll by toggle("retain-scroll") { true }
		val scrollSpeed by integer("scroll-speed", 1, 50) { 10 }
		val inverseScroll by toggle("inverse-scroll") { false }
		val padding by integer("padding", 1, 20) { 5 }
		val margin by integer("margin", 1, 60) { 20 }
		val itemsBlockScrolling by toggle("block-item-scrolling") { true }
		val highlightSearchResults by toggle("highlight-search-results") { true }
		val highlightSearchResultsColour by colour("highlight-search-results-colour") {
			ChromaColour.fromRGB(
				0,
				176,
				0,
				0,
				255
			)
		}
	}

	@Subscribe
	fun highlightSlots(event: SlotRenderEvents.Before) {
		if (!TConfig.highlightSearchResults) return
		val storageOverlayScreen =
			(MC.screen as? StorageOverlayScreen)
				?: (MC.handledScreen?.customGui as? StorageOverlayCustom)?.overview
				?: return
		val stack = event.slot.item ?: return
		val search = StorageOverlayScreen.searchText.get().takeIf { it.isNotBlank() } ?: return
		if (storageOverlayScreen.matchesSearch(stack, search)) {
			event.context.fill(
				event.slot.x,
				event.slot.y,
				event.slot.x + 16,
				event.slot.y + 16,
				TConfig.highlightSearchResultsColour.getEffectiveColourRGB()
			)
		}
	}


	fun adjustScrollSpeed(amount: Double): Double {
		return amount * TConfig.scrollSpeed * (if (TConfig.inverseScroll) 1 else -1)
	}

	var lastStorageOverlay: StorageOverviewScreen? = null
	var skipNextStorageOverlayBackflip = false
	var currentHandler: StorageBackingHandle? = null

	@Subscribe
	fun onChestContentUpdate(event: ChestInventoryUpdateEvent) {
		rememberContent(currentHandler)
	}

	@Subscribe
	fun onClick(event: SlotClickEvent) {
		if (lastStorageOverlay != null && event.slot.container !is Inventory && event.slot.containerSlot < 9
			&& event.stack.item != Items.BLACK_STAINED_GLASS_PANE
		) {
			skipNextStorageOverlayBackflip = true
		}
	}

	@Subscribe
	fun onScreenChange(it: ScreenChangeEvent) {
		if (it.old == null && it.new == null) return
		val storageOverlayScreen = it.old as? StorageOverlayScreen
			?: ((it.old as? AbstractContainerScreen<*>)?.customGui as? StorageOverlayCustom)?.overview
		var storageOverviewScreen = it.old as? StorageOverviewScreen
		val screen = it.new as? ContainerScreen
		rememberContent(currentHandler)
		val oldHandler = currentHandler
		currentHandler = StorageBackingHandle.fromScreen(screen)
		if (storageOverviewScreen != null && oldHandler is StorageBackingHandle.HasBackingScreen) {
			val player = MC.player
			assert(player != null)
			player?.connection?.send(ServerboundContainerClosePacket(oldHandler.handler.containerId))
			if (player?.containerMenu === oldHandler.handler) {
				player.containerMenu = player.inventoryMenu
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
		if (storageOverlayScreen?.isExiting == true) return
		screen.customGui = StorageOverlayCustom(
			currentHandler ?: return,
			screen,
			storageOverlayScreen ?: (if (TConfig.alwaysReplace) StorageOverlayScreen() else return)
		)
	}

	fun rememberContent(handler: StorageBackingHandle?) {
		handler ?: return
		val data = Data.data.storageInventories
		when (handler) {
			is StorageBackingHandle.Overview -> rememberStorageOverview(handler, data)
			is StorageBackingHandle.Page -> rememberPage(handler, data)
		}
	}

	private fun rememberStorageOverview(
		handler: StorageBackingHandle.Overview,
		data: SortedMap<StoragePageSlot, StorageData.StorageInventory>
	) {
		for ((index, stack) in handler.handler.items.withIndex()) { // TODO: replace with slot iteration
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
				data[slot] = StorageData.StorageInventory(slot, null)
			}
		}
		Data.markDirty()
	}

	private fun rememberPage(
		handler: StorageBackingHandle.Page,
		data: SortedMap<StoragePageSlot, StorageData.StorageInventory>
	) {
		val newStacks =
			VirtualInventory(handler.handler.items.take(handler.handler.rowCount * 9).drop(9).map { it.copy() })
		data.compute(handler.storagePageSlot) { slot, existingInventory ->
			(existingInventory ?: StorageData.StorageInventory(
				slot,
				null
			)).also {
				it.inventory = newStacks
			}
		}
		Data.markDirty(newStacks.serializationCache.discard())
	}
}
