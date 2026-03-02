package moe.nea.firmament.features.inventory

import io.github.notenoughupdates.moulconfig.gui.KeyboardEvent
import io.github.notenoughupdates.moulconfig.gui.MouseEvent
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket
import net.minecraft.world.item.ItemStack
import moe.nea.firmament.Firmament
import moe.nea.firmament.mixins.accessor.AccessorSignEditScreen
import moe.nea.firmament.repo.ExpensiveItemCacheApi
import moe.nea.firmament.repo.HypixelStaticData
import moe.nea.firmament.repo.ItemCache.asItemStack
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.removeColorCodes

class SearchOverlayScreen(
    private val originalSignScreen: AbstractSignEditScreen,
    private val searchType: SearchOverlay.SearchType
) : Screen(Component.literal("Search")) {

    companion object {
        const val MAX_SUGGESTIONS = 5
    }

    data class SearchSuggestion(
        val skyblockId: SkyblockId,
        val displayName: String,
        val cleanName: String,
        val itemStack: ItemStack?
    )

    private var searchText: String = ""
    private var suggestions: List<SearchSuggestion> = emptyList()
    private var selectedIndex: Int = 0
    private var cursorPosition: Int = 0

    private val panelWidth = 250
    private val labelHeight = 12
    private val textFieldHeight = 20
    private val suggestionHeight = 20
    private val padding = 8

    private val panelX get() = (width - panelWidth) / 2
    private val panelY get() = (height - totalPanelHeight) / 2
    private val totalPanelHeight: Int
        get() = labelHeight + textFieldHeight + padding * 2 + (if (suggestions.isNotEmpty()) suggestions.size * suggestionHeight else 0)

    @OptIn(ExpensiveItemCacheApi::class)
    private fun updateSuggestions() {
        if (searchText.isBlank()) {
            suggestions = emptyList()
            selectedIndex = 0
            return
        }

        val query = searchText.lowercase().trim()
        val results = mutableListOf<Pair<SearchSuggestion, Int>>()

        // debug Log data availability
        val bazaarCount = HypixelStaticData.bazaarData.size
        val ahCount = HypixelStaticData.lowestBin.size
        Firmament.logger.info("Search type: $searchType, Bazaar items: $bazaarCount, AH items: $ahCount")

        // search all items in the repository
        val items = RepoManager.neuRepo.items?.items?.values ?: return

        for (neuItem in items) {
            val skyblockId = SkyblockId(neuItem.skyblockItemId)
            val isAvailable = when (searchType) {
                SearchOverlay.SearchType.BAZAAR -> {
                    HypixelStaticData.hasBazaarStock(SkyblockId.BazaarStock.fromSkyBlockId(skyblockId))
                }
                SearchOverlay.SearchType.AUCTION_HOUSE -> {
                    HypixelStaticData.hasAuctionHouseOffers(skyblockId)
                }
            }

            if (!isAvailable) continue

            val displayName = neuItem.displayName
            val cleanName = displayName.removeColorCodes()
            val cleanNameLower = cleanName.lowercase()

            // create a score for the the match
            val score = when {
                cleanNameLower == query -> 1000
                cleanNameLower.startsWith(query) -> 500 + (100 - cleanName.length).coerceAtLeast(0) // starts with, shorter names
                cleanNameLower.contains(query) -> 100 + (100 - cleanName.length).coerceAtLeast(0) // contains, prefer shorter names
                else -> {
                    val queryWords = query.split(" ").filter { it.isNotBlank() }
                    val nameWords = cleanNameLower.split(" ").filter { it.isNotBlank() }

                    val allMatch = queryWords.all { qw ->
                        nameWords.any { nw -> nw.startsWith(qw) }
                    }

                    if (allMatch && queryWords.isNotEmpty()) {
                        50 + queryWords.size * 10
                    } else {
                        -1 // No match
                    }
                }
            }

            if (score > 0) {
                val itemStack = neuItem.asItemStack()
                results.add(SearchSuggestion(skyblockId, displayName, cleanName, itemStack) to score)
            }
        }

        // Sort by score descending so take top results
        suggestions = results
            .sortedByDescending { it.second }
            .take(MAX_SUGGESTIONS)
            .map { it.first }

        selectedIndex = if (suggestions.isNotEmpty()) 0 else -1
    }

    private fun submitSearch(query: String) {
        Firmament.logger.info("Submitting search: $query")

        val accessor = originalSignScreen as AccessorSignEditScreen

        val sign = accessor.sign_Firmament
        val blockPos = sign.blockPos

        val messages = accessor.messages_Firmament
        messages[0] = query

        val connection = MC.player?.connection
        if (connection != null) {
            val packet = ServerboundSignUpdatePacket(
                blockPos,
                true, // isFrontText
                messages[0],
                messages.getOrElse(1) { "" },
                messages.getOrElse(2) { "" },
                messages.getOrElse(3) { "" }
            )
            connection.send(packet)
            Firmament.logger.info("sent sign update packet with query: $query")
        } else {
            Firmament.logger.warn("cant send sign update packet")
        }

        MC.instance.setScreen(null)
    }

    private fun selectSuggestion(suggestion: SearchSuggestion) {
        submitSearch(suggestion.cleanName)
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        context.fill(0, 0, width, height, 0x80000000.toInt())

        val x = panelX
        var y = panelY

        val bgHeight = labelHeight + textFieldHeight + padding * 2 + (if (suggestions.isNotEmpty()) suggestions.size * suggestionHeight else 0)
        context.fill(x, y, x + panelWidth, y + bgHeight, 0xFFC6C6C6.toInt()) // Light gray like vanilla

        context.fill(x, y, x + panelWidth, y + 1, 0xFF373737.toInt()) // Top border dark
        context.fill(x, y, x + 1, y + bgHeight, 0xFF373737.toInt()) // Left border dark
        context.fill(x + panelWidth - 1, y, x + panelWidth, y + bgHeight, 0xFFFFFFFF.toInt()) // Right border light
        context.fill(x, y + bgHeight - 1, x + panelWidth, y + bgHeight, 0xFFFFFFFF.toInt()) // Bottom border light

        y += padding

        val typeLabel = when (searchType) {
            SearchOverlay.SearchType.BAZAAR -> "Bazaar Search"
            SearchOverlay.SearchType.AUCTION_HOUSE -> "Auction House Search"
        }
        context.drawString(MC.font, typeLabel, x + padding, y, 0xFF000000.toInt(), false)
        y += 12

        val textFieldX = x + padding
        val textFieldWidth = panelWidth - padding * 2
        context.fill(textFieldX - 1, y - 1, textFieldX + textFieldWidth + 1, y + textFieldHeight + 1, 0xFFA0A0A0.toInt()) // Border
        context.fill(textFieldX, y, textFieldX + textFieldWidth, y + textFieldHeight, 0xFF000000.toInt()) // Background

        val displayText = searchText.ifEmpty { "" }
        context.drawString(MC.font, displayText, textFieldX + 4, y + 6, 0xFFFFFFFF.toInt(), false)

        if (System.currentTimeMillis() % 1000 < 500) {
            val cursorX = textFieldX + 4 + MC.font.width(searchText.take(cursorPosition))
            context.fill(cursorX, y + 4, cursorX + 1, y + textFieldHeight - 4, 0xFFFFFFFF.toInt())
        }

        y += textFieldHeight

        for ((index, suggestion) in suggestions.withIndex()) {
            val suggestionY = y + index * suggestionHeight
            val isSelected = index == selectedIndex
            val isHovered = mouseX >= x && mouseX <= x + panelWidth &&
                mouseY >= suggestionY && mouseY <= suggestionY + suggestionHeight

            val bgColor = when {
                isSelected -> 0xFF4080FF.toInt() // Blue for selected
                isHovered -> 0xFFAAAAAA.toInt() // Lighter gray for hovered
                else -> 0xFFC6C6C6.toInt() // Same as panel
            }
            context.fill(x + 1, suggestionY, x + panelWidth - 1, suggestionY + suggestionHeight, bgColor)

            suggestion.itemStack?.let { itemStack ->
                context.renderItem(itemStack, x + 4, suggestionY + 2)
            }

            val textColor = if (isSelected) 0xFFFFFFFF.toInt() else 0xFF000000.toInt()
            context.drawString(
                MC.font,
                suggestion.cleanName,
                x + 24,
                suggestionY + 6,
                textColor,
                false
            )
        }
    }

    override fun mouseClicked(click: MouseButtonEvent, doubled: Boolean): Boolean {
        val x = panelX
        val y = panelY + padding + labelHeight + textFieldHeight

        for ((index, suggestion) in suggestions.withIndex()) {
            val suggestionY = y + index * suggestionHeight
            if (click.x >= x && click.x <= x + panelWidth &&
                click.y >= suggestionY && click.y <= suggestionY + suggestionHeight
            ) {
                selectSuggestion(suggestion)
                return true
            }
        }

        return super.mouseClicked(click, doubled)
    }

    override fun mouseReleased(click: MouseButtonEvent): Boolean {
        return super.mouseReleased(click)
    }

    override fun charTyped(input: CharacterEvent): Boolean {
        val char = input.codepointAsString()
        if (char.isNotEmpty() && char[0].code >= 32) {
            searchText = searchText.substring(0, cursorPosition) + char + searchText.substring(cursorPosition)
            cursorPosition += char.length
            updateSuggestions()
            return true
        }
        return super.charTyped(input)
    }

    override fun keyPressed(input: KeyEvent): Boolean {
        val keyCode = input.input()

		// all key stuff
        when (keyCode) {
            257 -> {
                if (suggestions.isNotEmpty() && selectedIndex in suggestions.indices) {
                    selectSuggestion(suggestions[selectedIndex])
                } else if (searchText.isNotBlank()) {
                    submitSearch(searchText)
                }
                return true
            }
            259 -> {
                if (cursorPosition > 0) {
                    searchText = searchText.substring(0, cursorPosition - 1) + searchText.substring(cursorPosition)
                    cursorPosition--
                    updateSuggestions()
                }
                return true
            }
            261 -> {
                if (cursorPosition < searchText.length) {
                    searchText = searchText.substring(0, cursorPosition) + searchText.substring(cursorPosition + 1)
                    updateSuggestions()
                }
                return true
            }
            263 -> {
                if (cursorPosition > 0) cursorPosition--
                return true
            }
            262 -> {
                if (cursorPosition < searchText.length) cursorPosition++
                return true
            }
            264 -> {
                if (suggestions.isNotEmpty()) {
                    selectedIndex = (selectedIndex + 1).coerceAtMost(suggestions.size - 1)
                    return true
                }
            }
            265 -> {
                if (suggestions.isNotEmpty()) {
                    selectedIndex = (selectedIndex - 1).coerceAtLeast(0)
                    return true
                }
            }
            258 -> {
                if (suggestions.isNotEmpty()) {
                    selectedIndex = (selectedIndex + 1) % suggestions.size
                    return true
                }
            }
        }

        return super.keyPressed(input)
    }

    override fun keyReleased(input: KeyEvent): Boolean {
        return super.keyReleased(input)
    }

    override fun onClose() {
        MC.instance.setScreen(null)
    }

    override fun shouldCloseOnEsc(): Boolean = true
}
