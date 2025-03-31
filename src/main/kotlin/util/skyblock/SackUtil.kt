package moe.nea.firmament.util.skyblock

import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ChestInventoryUpdateEvent
import moe.nea.firmament.events.ProcessChatEvent
import moe.nea.firmament.repo.ItemNameLookup
import moe.nea.firmament.util.SHORT_NUMBER_FORMAT
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.data.ProfileSpecificDataHolder
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.iterableView
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.parseShortNumber
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.unformattedString
import moe.nea.firmament.util.useMatch

object SackUtil {
	@Serializable
	data class SackContents(
		// TODO: store the certainty of knowledge for each item.
		val contents: MutableMap<SkyblockId, Long> = mutableMapOf(),
//		val sackTypes:
	)

	object Store : ProfileSpecificDataHolder<SackContents>(serializer(), "Sacks", ::SackContents)

	val items get() = Store.data?.contents ?: mutableMapOf()
	val storedRegex = "^Stored: (?<stored>$SHORT_NUMBER_FORMAT)/(?<max>$SHORT_NUMBER_FORMAT)$".toPattern()

	@Subscribe
	fun storeDataFromInventory(event: ChestInventoryUpdateEvent) {
		val screen = event.inventory as? GenericContainerScreen ?: return
		if (!screen.title.unformattedString.endsWith(" Sack")) return
		val inv = screen.screenHandler?.inventory ?: return
		if (inv.size() < 18) return
		val backSlot = inv.getStack(inv.size() - 5)
		if (backSlot.displayNameAccordingToNbt.unformattedString != "Go Back") return
		if (backSlot.loreAccordingToNbt.map { it.unformattedString } != listOf("To Sack of Sacks")) return
		for (itemStack in inv.iterableView) {
			// TODO: handle runes and gemstones
			val stored = itemStack.loreAccordingToNbt.firstNotNullOfOrNull {
				storedRegex.useMatch(it.unformattedString) {
					val stored = parseShortNumber(group("stored")).toLong()
					val max = parseShortNumber(group("max")).toLong()
					stored
				}
			} ?: continue
			val itemId = itemStack.skyBlockId ?: continue
			items[itemId] = stored
		}
		Store.markDirty()
	}

	@Subscribe
	fun updateFromChat(event: ProcessChatEvent) {
		if (!event.unformattedString.startsWith("[Sacks]")) return
		getUpdatesFromMessage(event.text)
	}

	fun getUpdatesFromMessage(text: Text): List<SackUpdate> {
		val update = ChatUpdate()
		text.siblings.forEach(update::updateFromHoverText)
		return update.updates
	}

	data class SackUpdate(
		val itemId: SkyblockId?,
		val itemName: String,
		val changeAmount: Long,
	)

	private class ChatUpdate {
		val updates = mutableListOf<SackUpdate>()
		var foundAdded = false
		var foundRemoved = false

		fun updateFromCleanText(cleanedText: String) {
			cleanedText.split("\n").forEach { line ->
				changePattern.useMatch(line) {
					val amount = parseShortNumber(group("amount")).toLong()
					val itemName = group("itemName")
					val itemId = ItemNameLookup.guessItemByName(itemName, false)
					updates.add(SackUpdate(itemId, itemName, amount))
				}
			}
		}

		fun updateFromHoverText(text: Text) {
			text.siblings.forEach(::updateFromHoverText)
			val hoverText = (text.style.hoverEvent as? HoverEvent.ShowText)?.value ?: return
			val cleanedText = hoverText.unformattedString
			if (cleanedText.startsWith("Added items:\n")) {
				if (!foundAdded) {
					updateFromCleanText(cleanedText)
					foundAdded = true
				}
			}
			if (cleanedText.startsWith("Removed items:\n")) {
				if (!foundRemoved) {
					updateFromCleanText(cleanedText)
					foundRemoved = true
				}
			}
		}

	}

	val changePattern = "  (?<amount>[+\\-]$SHORT_NUMBER_FORMAT) (?<itemName>[^(]+) \\(.*\\)".toPattern()
}
