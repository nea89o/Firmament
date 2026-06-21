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
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics
import me.shedaniel.rei.api.client.gui.widgets.Tooltip
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext
import me.shedaniel.rei.api.common.entry.EntryStack
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.client.Minecraft
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag
import net.minecraft.network.chat.Component
import moe.nea.firmament.events.ItemTooltipEvent
import moe.nea.firmament.repo.ExpensiveItemCacheApi
import moe.nea.firmament.repo.ItemCache
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.darkGrey
import moe.nea.firmament.util.mc.RequiresComponents
import moe.nea.firmament.util.mc.accessor
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt

// TODO: make this re implement BatchedEntryRenderer, if possible (likely not, due to no-alloc rendering)
// Also it is probably not even that much faster now, with render layers.
object NEUItemEntryRenderer : EntryRenderer<SBItemStack> {
	@OptIn(ExpensiveItemCacheApi::class, RequiresComponents::class)
	override fun render(
		entry: EntryStack<SBItemStack>,
		context: GuiGraphics,
		bounds: Rectangle,
		mouseX: Int,
		mouseY: Int,
		delta: Float
	) {
		val neuItem = entry.value.neuItem
		val itemToRender = if(!RepoManager.TConfig.perfectRenders.rendersPerfectVisuals() && !entry.value.isWarm() && neuItem != null) {
			ItemCache.recacheSoon(neuItem)
			ItemStack(Items.PAINTING)
		} else {
			entry.value.asImmutableItemStack().upgrade()
		}

		context.pose().pushMatrix()
		context.pose().translate(bounds.centerX.toFloat(), bounds.centerY.toFloat())
		context.pose().scale(bounds.width.toFloat() / 16F, bounds.height.toFloat() / 16F)
		context.renderItem(itemToRender, -8, -8)
		context.renderItemDecorations(
			MC.font, itemToRender, -8, -8,
			if (entry.value.getStackSize() > 1000) FirmFormatters.shortFormat(
				entry.value.getStackSize()
					.toDouble()
			)
			else null
		)
		context.pose().popMatrix()
	}

	val minecraft = Minecraft.getInstance()
	var canUseVanillaTooltipEvents = true

	@OptIn(ExpensiveItemCacheApi::class, RequiresComponents::class)
	override fun getTooltip(entry: EntryStack<SBItemStack>, tooltipContext: TooltipContext): Tooltip? {
		if (!entry.value.isWarm() && !RepoManager.TConfig.perfectRenders.rendersPerfectText()) {
			val neuItem = entry.value.neuItem
			if (neuItem != null) {
				val lore = mutableListOf<Component>()
				lore.add(Component.literal(neuItem.displayName))
				neuItem.lore.mapTo(mutableListOf()) { Component.literal(it) }
				return Tooltip.create(lore)
			}
		}

		val stack = entry.value.asImmutableItemStack().upgrade() // TODO: this shouldnt upgrade if possible, or check for upgradability, otherwise we risk a too fast repo, with a too slow REI tooltip indexer crashing us here

		val lore = mutableListOf(stack.accessor().displayNameAccordingToNbt)
		lore.addAll(stack.accessor().loreAccordingToNbt)
		if (canUseVanillaTooltipEvents) {
			try {
				ItemTooltipCallback.EVENT.invoker().getTooltip(
					stack, tooltipContext.vanillaContext(), TooltipFlag.Default.NORMAL, lore
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
					TooltipFlag.Default.NORMAL,
					lore
				)
			)
		}
		if (entry.value.getStackSize() > 1000 && lore.isNotEmpty())
			lore.add(1, Component.literal("${entry.value.getStackSize()}x").darkGrey())
		// TODO: tags aren't sent as early now so some tooltip components that use tags will crash the game
//		stack.getTooltip(
//			Item.TooltipContext.create(
//				tooltipContext.vanillaContext().registryLookup
//					?: MC.defaultRegistries
//			),
//			MC.player,
//			TooltipFlag.Default.NORMAL
//		)
		return Tooltip.create(lore)
	}


}
