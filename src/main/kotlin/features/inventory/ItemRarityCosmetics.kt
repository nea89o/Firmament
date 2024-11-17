package moe.nea.firmament.features.inventory

import java.awt.Color
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.item.ItemStack
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HotbarItemRenderEvent
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.collections.lastNotNullOfOrNull
import moe.nea.firmament.util.collections.memoizeIdentity
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.skyblock.Rarity
import moe.nea.firmament.util.unformattedString

object ItemRarityCosmetics : FirmamentFeature {
	override val identifier: String
		get() = "item-rarity-cosmetics"

	object TConfig : ManagedConfig(identifier, Category.INVENTORY) {
		val showItemRarityBackground by toggle("background") { false }
		val showItemRarityInHotbar by toggle("background-hotbar") { false }
	}

	override val config: ManagedConfig
		get() = TConfig

	private val rarityToColor = mapOf(
		Rarity.UNCOMMON to Formatting.GREEN,
		Rarity.COMMON to Formatting.WHITE,
		Rarity.RARE to Formatting.DARK_BLUE,
		Rarity.EPIC to Formatting.DARK_PURPLE,
		Rarity.LEGENDARY to Formatting.GOLD,
		Rarity.MYTHIC to Formatting.LIGHT_PURPLE,
		Rarity.DIVINE to Formatting.BLUE,
		Rarity.SPECIAL to Formatting.DARK_RED,
		Rarity.VERY_SPECIAL to Formatting.DARK_RED,
		Rarity.SUPREME to Formatting.DARK_RED,
	).mapValues {
		val c = Color(it.value.colorValue!!)
		c.rgb
	}

	fun drawItemStackRarity(drawContext: DrawContext, x: Int, y: Int, item: ItemStack) {
		val rarity = Rarity.fromItem(item) ?: return
		val rgb = rarityToColor[rarity] ?: 0xFF00FF80.toInt()
		drawContext.drawGuiTexture(
			RenderLayer::getGuiTextured,
			Identifier.of("firmament:item_rarity_background"),
			x, y,
			16, 16,
			rgb
		)
	}


	@Subscribe
	fun onRenderSlot(it: SlotRenderEvents.Before) {
		if (!TConfig.showItemRarityBackground) return
		val stack = it.slot.stack ?: return
		drawItemStackRarity(it.context, it.slot.x, it.slot.y, stack)
	}

	@Subscribe
	fun onRenderHotbarItem(it: HotbarItemRenderEvent) {
		if (!TConfig.showItemRarityInHotbar) return
		val stack = it.item
		drawItemStackRarity(it.context, it.x, it.y, stack)
	}
}
