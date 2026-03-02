package moe.nea.firmament.features.inventory

import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ScreenChangeEvent
import moe.nea.firmament.Firmament
import moe.nea.firmament.mixins.accessor.AccessorSignEditScreen
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.unformattedString

object SearchOverlay
{

    val identifier: String
        get() = "search-overlay"

    @Config
    object TConfig : ManagedConfig(identifier, Category.INVENTORY)
	{
        val enableBazaarOverlay by toggle("bazaar") { true }
        val enableAuctionHouseOverlay by toggle("auction-house") { true }
    }

    enum class SearchType
	{
        BAZAAR,
        AUCTION_HOUSE
    }

    private var lastContainerScreen: ContainerScreen? = null
    private var lastContainerTitle: String = ""

    private fun isBazaarScreen(screen: ContainerScreen): Boolean
	{
        val title = screen.title.unformattedString
        return title.contains("Bazaar") || title.contains("bazaar")
    }

    private fun isAuctionHouseScreen(screen: ContainerScreen): Boolean
	{
        val title = screen.title.unformattedString
        return title.contains("Auction") || title.contains("auction") ||
            title.contains("Browser") || title.contains("BIN")
    }

    fun detectSearchType(screen: AbstractSignEditScreen): SearchType? {
        val accessor = screen as AccessorSignEditScreen
        val messages = accessor.messages_Firmament
        val combinedText = messages.joinToString(" ").lowercase()

        if (!combinedText.contains("enter query") &&
            !combinedText.contains("enter item") &&
            !combinedText.contains("search")) {
            return null
        }

        val lastScreen = lastContainerScreen
        val lastTitle = lastContainerTitle.lowercase()

        Firmament.logger.info("Detecting search type - last screen title: $lastTitle")

        return when {
            // check last screen title for bazaar/ah indicators
            lastTitle.contains("bazaar") -> {
                if (TConfig.enableBazaarOverlay) SearchType.BAZAAR else null
            }
            lastTitle.contains("auction") || lastTitle.contains("browser") || lastTitle.contains("bin") -> {
                if (TConfig.enableAuctionHouseOverlay) SearchType.AUCTION_HOUSE else null
            }
            combinedText.contains("bazaar") -> {
                if (TConfig.enableBazaarOverlay) SearchType.BAZAAR else null
            }
            combinedText.contains("auction") -> {
                if (TConfig.enableAuctionHouseOverlay) SearchType.AUCTION_HOUSE else null
            }
            else -> {
                when {
                    TConfig.enableBazaarOverlay -> SearchType.BAZAAR
                    TConfig.enableAuctionHouseOverlay -> SearchType.AUCTION_HOUSE
                    else -> null
                }
            }
        }
    }

    @Subscribe
    fun onScreenChange(event: ScreenChangeEvent) {
        if (!SBData.isOnSkyblock) return

        val newScreen = event.new
        val oldScreen = event.old

        // debug: log all screen transitions
        val oldName = oldScreen?.let { it::class.simpleName + ": " + (it.title?.string ?: "no title") } ?: "null"
        val newName = newScreen?.let { it::class.simpleName + ": " + (it.title?.string ?: "no title") } ?: "null"
        Firmament.logger.info("Screen change: $oldName -> $newName")

        if (oldScreen is ContainerScreen) {
            lastContainerScreen = oldScreen
            lastContainerTitle = oldScreen.title.unformattedString
            Firmament.logger.info("Tracked container screen (old): $lastContainerTitle")
        }
        if (newScreen is ContainerScreen) {
            lastContainerScreen = newScreen
            lastContainerTitle = newScreen.title.unformattedString
            Firmament.logger.info("Tracked container screen (new): $lastContainerTitle")
        }

        if (newScreen !is AbstractSignEditScreen) return

        Firmament.logger.info("Sign screen detected. Last container was: '$lastContainerTitle'")

        val searchType = detectSearchType(newScreen)
        if (searchType == null) return

        Firmament.logger.info("Detected $searchType search sign - replacing with overlay")

        // custom search screen
        val searchScreen = SearchOverlayScreen(newScreen, searchType)
        event.overrideScreen = searchScreen
    }
}
