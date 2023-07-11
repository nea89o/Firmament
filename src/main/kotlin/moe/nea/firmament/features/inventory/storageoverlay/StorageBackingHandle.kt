package moe.nea.firmament.features.inventory.storageoverlay

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.screen.GenericContainerScreenHandler
import moe.nea.firmament.util.ifMatches
import moe.nea.firmament.util.unformattedString

/**
 * A handle representing the state of the "server side" screens.
 */
sealed interface StorageBackingHandle {

    sealed interface HasBackingScreen {
        val handler: GenericContainerScreenHandler
    }

    /**
     * No open "server side" screen.
     */
    object None : StorageBackingHandle

    /**
     * The main storage overview is open. Clicking on a slot will open that page. This page is accessible via `/storage`
     */
    data class Overview(override val handler: GenericContainerScreenHandler) : StorageBackingHandle, HasBackingScreen

    /**
     * An individual storage page is open. This may be a backpack or an enderchest page. This page is accessible via
     * the [Overview] or via `/ec <index + 1>` for enderchest pages.
     */
    data class Page(override val handler: GenericContainerScreenHandler, val storagePageSlot: StoragePageSlot) :
        StorageBackingHandle, HasBackingScreen

    companion object {
        private val enderChestName = "^Ender Chest \\(([1-9])/[1-9]\\)$".toRegex()
        private val backPackName = "^.+Backpack \\(Slot #([0-9]+)\\)$".toRegex()

        /**
         * Parse a screen into a [StorageBackingHandle]. If this returns null it means that the screen is not
         * representable as a [StorageBackingHandle], meaning another screen is open, for example the enderchest icon
         * selection screen.
         */
        fun fromScreen(screen: Screen?): StorageBackingHandle? {
            if (screen == null) return None
            if (screen !is GenericContainerScreen) return null
            val title = screen.title.unformattedString
            if (title == "Storage") return Overview(screen.screenHandler)
            return title.ifMatches(enderChestName) {
                Page(screen.screenHandler, StoragePageSlot.ofEnderChestPage(it.groupValues[1].toInt()))
            } ?: title.ifMatches(backPackName) {
                Page(screen.screenHandler, StoragePageSlot.ofBackPackPage(it.groupValues[1].toInt()))
            }
        }
    }
}
