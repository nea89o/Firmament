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
import net.minecraft.item.tooltip.TooltipType
import moe.nea.firmament.compat.rei.FirmamentReiPlugin.Companion.asItemEntry
import moe.nea.firmament.events.ItemTooltipEvent
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt

// TODO: make this re implement BatchedEntryRenderer, if possible (likely not, due to no-alloc rendering)
// Also it is probably not even that much faster now, with render layers.
object NEUItemEntryRenderer : EntryRenderer<SBItemStack> {
	override fun render(
		entry: EntryStack<SBItemStack>,
		context: DrawContext,
		bounds: Rectangle,
		mouseX: Int,
		mouseY: Int,
		delta: Float
	) {
		context.matrices.push()
		context.matrices.translate(bounds.centerX.toFloat(), bounds.centerY.toFloat(), 0F)
		context.matrices.scale(bounds.width.toFloat() / 16F, bounds.height.toFloat() / 16F, 1f)
		context.drawItemWithoutEntity(
			entry.asItemEntry().value,
			-8, -8,
		)
		context.matrices.pop()
	}

	val minecraft = MinecraftClient.getInstance()
	var canUseVanillaTooltipEvents = true

	override fun getTooltip(entry: EntryStack<SBItemStack>, tooltipContext: TooltipContext): Tooltip? {
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
			ItemTooltipEvent.publish(ItemTooltipEvent(
				stack,
				tooltipContext.vanillaContext(),
				TooltipType.BASIC,
				lore
			))
		}
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
