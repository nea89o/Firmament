/*
 * SPDX-FileCopyrightText: 2018-2023 shedaniel <daniel@shedaniel.me>
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 * SPDX-License-Identifier: MIT
 */

package moe.nea.firmament.compat.rei

import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer
import me.shedaniel.rei.api.client.gui.widgets.Tooltip
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext
import me.shedaniel.rei.api.common.entry.EntryStack
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.text.Text
import moe.nea.firmament.events.ItemTooltipEvent
import moe.nea.firmament.repo.ExpensiveItemCacheApi
import moe.nea.firmament.repo.ItemCache
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.darkGrey
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt

// TODO: make this re implement BatchedEntryRenderer, if possible (likely not, due to no-alloc rendering)
// Also it is probably not even that much faster now, with render layers.
object NEUItemEntryRenderer : EntryRenderer<SBItemStack> {
	@OptIn(ExpensiveItemCacheApi::class)
	override fun render(
		entry: EntryStack<SBItemStack>,
		context: DrawContext,
		bounds: Rectangle,
		mouseX: Int,
		mouseY: Int,
		delta: Float
	) {
		val neuItem = entry.value.neuItem
		val itemToRender = if(!RepoManager.Config.perfectRenders.rendersPerfectVisuals() && !entry.value.isWarm() && neuItem != null) {
			ItemCache.recacheSoon(neuItem)
			ItemStack(Items.PAINTING)
		} else {
			entry.value.asImmutableItemStack()
		}

		context.matrices.push()
		context.matrices.translate(bounds.centerX.toFloat(), bounds.centerY.toFloat(), 0F)
		context.matrices.scale(bounds.width.toFloat() / 16F, bounds.height.toFloat() / 16F, 1f)
		context.drawItemWithoutEntity(itemToRender, -8, -8)
		context.drawStackOverlay(
			minecraft.textRenderer, itemToRender, -8, -8,
			if (entry.value.getStackSize() > 1000) FirmFormatters.shortFormat(
				entry.value.getStackSize()
					.toDouble()
			)
			else null
		)
		context.matrices.pop()
	}

	val minecraft = MinecraftClient.getInstance()
	var canUseVanillaTooltipEvents = true

	@OptIn(ExpensiveItemCacheApi::class)
	override fun getTooltip(entry: EntryStack<SBItemStack>, tooltipContext: TooltipContext): Tooltip? {
		if (!entry.value.isWarm() && !RepoManager.Config.perfectRenders.rendersPerfectText()) {
			val neuItem = entry.value.neuItem
			if (neuItem != null) {
				val lore = mutableListOf<Text>()
				lore.add(Text.literal(neuItem.displayName))
				neuItem.lore.mapTo(mutableListOf()) { Text.literal(it) }
				return Tooltip.create(lore)
			}
		}

		val stack = entry.value.asImmutableItemStack()

		val lore = mutableListOf(stack.displayNameAccordingToNbt)
		lore.addAll(stack.loreAccordingToNbt)
		if (canUseVanillaTooltipEvents) {
			try {
				ItemTooltipCallback.EVENT.invoker().getTooltip(
					stack, tooltipContext.vanillaContext(), TooltipType.BASIC, lore
				)
			} catch (ex: Exception) {
				canUseVanillaTooltipEvents = false
				ErrorUtil.softError("Failed to use vanilla tooltips", ex)
			}
		} else {
			ItemTooltipEvent.publish(
				ItemTooltipEvent(
					stack,
					tooltipContext.vanillaContext(),
					TooltipType.BASIC,
					lore
				)
			)
		}
		if (entry.value.getStackSize() > 1000 && lore.isNotEmpty())
			lore.add(1, Text.literal("${entry.value.getStackSize()}x").darkGrey())
		// TODO: tags aren't sent as early now so some tooltip components that use tags will crash the game
//		stack.getTooltip(
//			Item.TooltipContext.create(
//				tooltipContext.vanillaContext().registryLookup
//					?: MC.defaultRegistries
//			),
//			MC.player,
//			TooltipType.BASIC
//		)
		return Tooltip.create(lore)
	}


}
