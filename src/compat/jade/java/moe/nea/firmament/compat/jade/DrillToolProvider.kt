package moe.nea.firmament.compat.jade

import java.util.Optional
import java.util.function.UnaryOperator
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.JadeIds
import snownee.jade.api.config.IPluginConfig
import snownee.jade.api.theme.IThemeHelper
import snownee.jade.api.ui.IElement
import snownee.jade.api.ui.IElementHelper
import snownee.jade.impl.ui.ItemStackElement
import snownee.jade.impl.ui.TextElement
import kotlin.jvm.optionals.getOrDefault
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f
import moe.nea.firmament.Firmament
import moe.nea.firmament.repo.ItemCache.asItemStack
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.MC

class DrillToolProvider : IBlockComponentProvider {
	override fun appendTooltip(
		tooltip: ITooltip,
		accessor: BlockAccessor,
		p2: IPluginConfig?
	) {
		val customBlock = CustomFakeBlockProvider.getCustomBlock(accessor) ?: return
		if (customBlock.breakingPower <= 0) return
		val tool = RepoManager.miningData.getToolsThatCanBreak(customBlock.breakingPower).firstOrNull()
			?.asItemStack() ?: return
		tooltip.replace(JadeIds.MC_HARVEST_TOOL, UnaryOperator { elements ->
			elements.map { inner ->
				val lastItemIndex = inner.indexOfLast { it is ItemStackElement }
				if (lastItemIndex < 0) return@map inner
				val innerMut = inner.toMutableList()
				val harvestIndicator = innerMut.indexOfLast {
					it is TextElement && it.cachedSize == Vec2f.ZERO && it.text.visit {
						if (it.isEmpty()) Optional.empty() else Optional.of(true)
					}.getOrDefault(false)
				}
				val canHarvest = (SBItemStack(MC.stackInHand).neuItem?.breakingPower ?: 0) >= customBlock.breakingPower
				val lastItem = innerMut[lastItemIndex] as ItemStackElement
				if (harvestIndicator < 0) {
					innerMut.add(lastItemIndex + 1, canHarvestIndicator(canHarvest, lastItem.alignment))
				} else {
					innerMut.set(harvestIndicator, canHarvestIndicator(canHarvest, lastItem.alignment))
				}
				innerMut.set(lastItemIndex, IElementHelper.get()
					.item(tool, 0.75f)
					.translate(lastItem.translation)
					.size(lastItem.size)
					.message(null)
					.align(lastItem.alignment))
				innerMut.subList(0, lastItemIndex - 1).removeIf { it is ItemStackElement }
				innerMut
			}
		})
	}

	fun canHarvestIndicator(canHarvest: Boolean, align: IElement.Align): IElement {
		val t = IThemeHelper.get()
		val text = if (canHarvest) t.success(CHECK) else t.danger(X)
		return IElementHelper.get().text(text)
			.scale(0.75F).zOffset(800).size(Vec2f.ZERO).translate(Vec2f(-3F, 3.25F)).align(align)
	}

	private val CHECK: Text = Text.literal("✔")
	private val X: Text = Text.literal("✕")

	override fun getUid(): Identifier {
		return Firmament.identifier("toolprovider")
	}
}
