package moe.nea.firmament.compat.jade

import snownee.jade.api.IWailaClientRegistration
import snownee.jade.api.ui.IElement
import snownee.jade.api.ui.IElementHelper
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.SkyBlockIsland
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.setSkyBlockId


fun String.jadeId(): Identifier = Identifier.of("firmament", this)

// This drill icon should work for CIT resource packs
val drillItem: ItemStack = Items.PRISMARINE_SHARD.defaultStack.setSkyBlockId(SkyblockId("TITANIUM_DRILL_1"))
val drillIcon: IElement = IElementHelper.get().item(drillItem, 0.5f).message(null)
fun IWailaClientRegistration.registerGemstone(type: String) {

}

fun isOnMiningIsland(): Boolean {
	if (!SBData.isOnSkyblock) return false
	// how does a when loop work
	if (SBData.skyblockLocation == SkyBlockIsland.forMode("dwarven_mines")) return true
	if (SBData.skyblockLocation == SkyBlockIsland.MINESHAFT) return true
	if (SBData.skyblockLocation == SkyBlockIsland.forMode("crystal_hollows")) return true
	if (SBData.skyblockLocation == SkyBlockIsland.forMode("crimson_isle")) return true
	return false
}
