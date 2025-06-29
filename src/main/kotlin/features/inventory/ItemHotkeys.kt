package moe.nea.firmament.features.inventory

import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.repo.ExpensiveItemCacheApi
import moe.nea.firmament.repo.HypixelStaticData
import moe.nea.firmament.repo.ItemCache
import moe.nea.firmament.repo.ItemCache.asItemStack
import moe.nea.firmament.repo.ItemCache.isBroken
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.asBazaarStock
import moe.nea.firmament.util.focusedItemStack
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.skyblock.SBItemUtil.getSearchName

object ItemHotkeys {
	object TConfig : ManagedConfig("item-hotkeys", Category.INVENTORY) {
		val openGlobalTradeInterface by keyBindingWithDefaultUnbound("global-trade-interface")
	}

	@OptIn(ExpensiveItemCacheApi::class)
	@Subscribe
	fun onHandledInventoryPress(event: HandledScreenKeyPressedEvent) {
		if (!event.matches(TConfig.openGlobalTradeInterface)) {
			return
		}
		var item = event.screen.focusedItemStack ?: return
		val skyblockId = item.skyBlockId ?: return
		item = RepoManager.getNEUItem(skyblockId)?.asItemStack()?.takeIf { !it.isBroken } ?: item
		if (HypixelStaticData.hasBazaarStock(skyblockId.asBazaarStock)) {
			MC.sendCommand("bz ${item.getSearchName()}")
		} else if (HypixelStaticData.hasAuctionHouseOffers(skyblockId)) {
			MC.sendCommand("ahs ${item.getSearchName()}")
		} else {
			return
		}
		event.cancel()
	}

}
