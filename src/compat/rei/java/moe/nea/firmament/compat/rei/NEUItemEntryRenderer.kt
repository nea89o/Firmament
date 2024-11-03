/*
 * SPDX-FileCopyrightText: 2018-2023 shedaniel <daniel@shedaniel.me>
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 * SPDX-License-Identifier: MIT
 */

package moe.nea.firmament.compat.rei

import com.mojang.blaze3d.platform.GlStateManager.DstFactor
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor
import com.mojang.blaze3d.systems.RenderSystem
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.entry.renderer.BatchedEntryRenderer
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer
import me.shedaniel.rei.api.client.gui.widgets.Tooltip
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext
import me.shedaniel.rei.api.common.entry.EntryStack
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.item.ModelTransformationMode
import moe.nea.firmament.compat.rei.FirmamentReiPlugin.Companion.asItemEntry
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt

object NEUItemEntryRenderer : EntryRenderer<SBItemStack>, BatchedEntryRenderer<SBItemStack, BakedModel> {
	override fun render(
		entry: EntryStack<SBItemStack>,
		context: DrawContext,
		bounds: Rectangle,
		mouseX: Int,
		mouseY: Int,
		delta: Float
	) {
		entry.asItemEntry().render(context, bounds, mouseX, mouseY, delta)
	}

	val minecraft = MinecraftClient.getInstance()

	override fun getTooltip(entry: EntryStack<SBItemStack>, tooltipContext: TooltipContext): Tooltip? {
		val stack = entry.value.asImmutableItemStack()

		val lore = mutableListOf(stack.displayNameAccordingToNbt)
		lore.addAll(stack.loreAccordingToNbt)

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

	override fun getExtraData(entry: EntryStack<SBItemStack>): BakedModel {
		return MC.itemRenderer.getModel(entry.asItemEntry().value,
		                                MC.world,
		                                MC.player, 0)

	}

	override fun getBatchIdentifier(entry: EntryStack<SBItemStack>, bounds: Rectangle?, extraData: BakedModel): Int {
		return 1738923 + if (extraData.isSideLit) 1 else 0
	}


	override fun startBatch(entryStack: EntryStack<SBItemStack>, e: BakedModel, drawContext: DrawContext, v: Float) {
		MC.textureManager.getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
			.setFilter(false, false)
		RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
		RenderSystem.enableBlend()
		RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA)
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
		if (!e.isSideLit) {
			DiffuseLighting.disableGuiDepthLighting()
		}
	}

	override fun renderBase(
		entryStack: EntryStack<SBItemStack>,
		model: BakedModel,
		drawContext: DrawContext,
		immediate: VertexConsumerProvider.Immediate,
		bounds: Rectangle,
		i: Int,
		i1: Int,
		v: Float
	) {
		if (entryStack.isEmpty) return
		drawContext.matrices.push()
		drawContext.matrices.translate(bounds.centerX.toDouble(), bounds.centerY.toDouble(), 0.0)
		// TODO: check the scaling here again
		drawContext.matrices.scale(
			bounds.width.toFloat(),
			(bounds.height + bounds.height) / -2F,
			(bounds.width + bounds.height) / 2f)
		MC.itemRenderer.renderItem(
			entryStack.value.asImmutableItemStack(),
			ModelTransformationMode.GUI,
			false, drawContext.matrices,
			immediate, LightmapTextureManager.MAX_LIGHT_COORDINATE,
			OverlayTexture.DEFAULT_UV,
			model
		)
		drawContext.matrices.pop()
	}

	override fun afterBase(entryStack: EntryStack<SBItemStack>?, e: BakedModel, drawContext: DrawContext?, v: Float) {
		RenderSystem.enableDepthTest()
		if (!e.isSideLit)
			DiffuseLighting.enableGuiDepthLighting()
	}

	override fun renderOverlay(
		entryStack: EntryStack<SBItemStack>,
		e: BakedModel,
		drawContext: DrawContext,
		immediate: VertexConsumerProvider.Immediate,
		bounds: Rectangle,
		i: Int,
		i1: Int,
		v: Float
	) {
		if (entryStack.isEmpty) return
		val modelViewStack = RenderSystem.getModelViewStack()
		modelViewStack.pushMatrix()
		modelViewStack.mul(drawContext.matrices.peek().positionMatrix)
		modelViewStack.translate(bounds.x.toFloat(), bounds.y.toFloat(), 0F)
		modelViewStack.scale(bounds.width / 16.0f,
		                     (bounds.width + bounds.height) / 2.0f / 16.0f,
		                     1.0f) // TODO: weird scale again
		drawContext.drawStackOverlay(MC.font, entryStack.value.asImmutableItemStack(), 0, 0, null)
		modelViewStack.popMatrix()
	}

	override fun endBatch(entryStack: EntryStack<SBItemStack>?, e: BakedModel?, drawContext: DrawContext?, v: Float) {
	}

}
