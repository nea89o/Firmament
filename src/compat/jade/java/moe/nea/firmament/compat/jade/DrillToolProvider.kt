package moe.nea.firmament.compat.jade

import java.util.function.UnaryOperator
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.JadeIds
import snownee.jade.api.config.IPluginConfig
import snownee.jade.api.theme.IThemeHelper
import snownee.jade.api.ui.Element
import snownee.jade.api.ui.JadeUI
import snownee.jade.gui.JadeLinearLayout
import snownee.jade.impl.ui.ItemStackElement
import snownee.jade.impl.ui.TextElementImpl
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import moe.nea.firmament.Firmament
import moe.nea.firmament.repo.ExpensiveItemCacheApi
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.MC

class DrillToolProvider : IBlockComponentProvider {
	@OptIn(ExpensiveItemCacheApi::class)
	override fun appendTooltip(
		tooltip: ITooltip,
		accessor: BlockAccessor,
		p2: IPluginConfig
	) {
		val customBlock = CustomFakeBlockProvider.getCustomBlock(accessor) ?: return
		val tool = RepoManager.miningData.getToolsThatCanBreak(customBlock.breakingPower).firstOrNull()
			?.asImmutableItemStack() ?: return
		tooltip.replace(JadeIds.MC_HARVEST_TOOL, UnaryOperator { elements ->
			elements.map { inner ->
				val lastItemIndex = inner.indexOfLast { it is ItemStackElement }
				if (lastItemIndex < 0) return@map inner
				val innerMut = inner.toMutableList()
				val harvestIndicator = innerMut.indexOfLast {
					it is TextElementImpl && it.width == 0 && it.string.isNotEmpty()
				}
				val canHarvest = SBItemStack(MC.stackInHand).breakingPower >= customBlock.breakingPower
				val lastItem = innerMut[lastItemIndex] as ItemStackElement
				if (harvestIndicator < 0) {
					innerMut.add(lastItemIndex + 1, canHarvestIndicator(canHarvest))
				} else {
					innerMut.set(harvestIndicator, canHarvestIndicator(canHarvest))
				}
				innerMut.set(
					lastItemIndex, JadeUI
						.item(tool, 0.75f)
				)
				innerMut.subList(0, lastItemIndex - 1).removeIf { it is ItemStackElement }
				innerMut
			}
		})
	}

	fun canHarvestIndicator(canHarvest: Boolean): Element {
		val t = IThemeHelper.get()
		val text = if (canHarvest) t.success(CHECK) else t.danger(X)
		return JadeUI.text(text)
			.scale(0.75F)
			.alignSelfCenter()
	}

	private val CHECK: Component = Component.literal("✔")
	private val X: Component = Component.literal("✕")

	override fun getUid(): Identifier {
		return Firmament.identifier("toolprovider")
	}
}
