package moe.nea.firmament.features.mining

import io.github.notenoughupdates.moulconfig.observer.ObservableList
import io.github.notenoughupdates.moulconfig.observer.Property
import io.github.notenoughupdates.moulconfig.platform.ModernItemStack
import io.github.notenoughupdates.moulconfig.xml.Bind
import net.minecraft.client.gui.screen.Screen
import net.minecraft.item.ItemStack
import moe.nea.firmament.repo.MiningRepoData
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.MoulConfigUtils
import moe.nea.firmament.util.SkyBlockIsland

object MiningBlockInfoUi {
	class MiningInfo(miningData: MiningRepoData) {
		@field:Bind("search")
		@JvmField
		var search = ""

		@get:Bind("ores")
		val blocks = miningData.customMiningBlocks.mapTo(ObservableList(mutableListOf())) { OreInfo(it, this) }
	}

	class OreInfo(block: MiningRepoData.CustomMiningBlock, info: MiningInfo) {
		@get:Bind("oreName")
		val oreName = block.name ?: "No Name"

		@get:Bind("blocks")
		val res = ObservableList(block.blocks189.map { BlockInfo(it, info) })
	}

	class BlockInfo(val block: MiningRepoData.Block189, val info: MiningInfo) {
		@get:Bind("item")
		val item = ModernItemStack.of(block.block?.let { ItemStack(it) } ?: ItemStack.EMPTY)

		@get:Bind("isSelected")
		val isSelected get() = info.search.let { block.isActiveIn(SkyBlockIsland.forMode(it)) }

		@get:Bind("itemName")
		val itemName get() = item.getDisplayName()

		@get:Bind("restrictions")
		val res = ObservableList(
			if (block.onlyIn != null)
				block.onlyIn.map { " §r- §a${it.userFriendlyName}" }
			else
				listOf("Everywhere")
		)
	}

	fun makeScreen(): Screen {
		return MoulConfigUtils.loadScreen("mining_block_info/index", MiningInfo(RepoManager.miningData), null)
	}
}
