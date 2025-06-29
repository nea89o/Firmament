package moe.nea.firmament.features.inventory

import org.lwjgl.glfw.GLFW
import net.minecraft.text.Text
import net.minecraft.util.StringIdentifiable
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ItemTooltipEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.repo.HypixelStaticData
import moe.nea.firmament.util.FirmFormatters.formatCommas
import moe.nea.firmament.util.asBazaarStock
import moe.nea.firmament.util.bold
import moe.nea.firmament.util.darkGrey
import moe.nea.firmament.util.gold
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.tr
import moe.nea.firmament.util.yellow

object PriceData : FirmamentFeature {
	override val identifier: String
		get() = "price-data"

	object TConfig : ManagedConfig(identifier, Category.INVENTORY) {
		val tooltipEnabled by toggle("enable-always") { true }
		val enableKeybinding by keyBindingWithDefaultUnbound("enable-keybind")
		val stackSizeKey by keyBinding("stack-size-keybind") { GLFW.GLFW_KEY_LEFT_SHIFT }
		val avgLowestBin by choice(
			"avg-lowest-bin-days",
		) {
			AvgLowestBin.THREEDAYAVGLOWESTBIN
		}
	}

	enum class AvgLowestBin : StringIdentifiable {
		OFF,
		ONEDAYAVGLOWESTBIN,
		THREEDAYAVGLOWESTBIN,
		SEVENDAYAVGLOWESTBIN;

		override fun asString(): String {
			return name
		}
	}

	override val config get() = TConfig

	fun formatPrice(label: Text, price: Double): Text {
		return Text.literal("")
			.yellow()
			.bold()
			.append(label)
			.append(": ")
			.append(
				Text.literal(formatCommas(price, fractionalDigits = 1))
					.append(if (price != 1.0) " coins" else " coin")
					.gold()
					.bold()
			)
	}

	@Subscribe
	fun onItemTooltip(it: ItemTooltipEvent) {
		if (!TConfig.tooltipEnabled) return
		if (TConfig.enableKeybinding.isBound && !TConfig.enableKeybinding.isPressed()) return
		val sbId = it.stack.skyBlockId
		val stackSize = it.stack.count
		val isShowingStack = TConfig.stackSizeKey.isPressed()
		val multiplier = if (isShowingStack) stackSize else 1
		val multiplierText =
			if (isShowingStack)
				tr("firmament.tooltip.multiply", "Showing prices for x${stackSize}").darkGrey()
			else
				tr(
					"firmament.tooltip.multiply.hint",
					"[${TConfig.stackSizeKey.format()}] to show x${stackSize}"
				).darkGrey()
		val bazaarData = HypixelStaticData.bazaarData[sbId?.asBazaarStock]
		val lowestBin = HypixelStaticData.lowestBin[sbId]
		val avgBinValue: Double? = when (TConfig.avgLowestBin) {
			AvgLowestBin.ONEDAYAVGLOWESTBIN -> HypixelStaticData.avg1dlowestBin[sbId]
			AvgLowestBin.THREEDAYAVGLOWESTBIN -> HypixelStaticData.avg3dlowestBin[sbId]
			AvgLowestBin.SEVENDAYAVGLOWESTBIN -> HypixelStaticData.avg7dlowestBin[sbId]
			AvgLowestBin.OFF -> null
		}
		if (bazaarData != null) {
			it.lines.add(Text.literal(""))
			it.lines.add(multiplierText)
			it.lines.add(
				formatPrice(
					tr("firmament.tooltip.bazaar.sell-order", "Bazaar Sell Order"),
					bazaarData.quickStatus.sellPrice * multiplier
				)
			)
			it.lines.add(
				formatPrice(
					tr("firmament.tooltip.bazaar.buy-order", "Bazaar Buy Order"),
					bazaarData.quickStatus.buyPrice * multiplier
				)
			)
		} else if (lowestBin != null) {
			it.lines.add(Text.literal(""))
			it.lines.add(multiplierText)
			it.lines.add(
				formatPrice(
					tr("firmament.tooltip.ah.lowestbin", "Lowest BIN"),
					lowestBin * multiplier
				)
			)
			if (avgBinValue != null) {
				it.lines.add(
					formatPrice(
						tr("firmament.tooltip.ah.avg-lowestbin", "AVG Lowest BIN"),
						avgBinValue * multiplier
					)
				)
			}
		}
	}
}
