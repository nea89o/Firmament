/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2018-2023 shedaniel <daniel@shedaniel.me>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 * SPDX-License-Identifier: MIT
 */

package moe.nea.firmament.rei

import com.mojang.blaze3d.platform.GlStateManager.DstFactor
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor
import com.mojang.blaze3d.systems.RenderSystem
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.entry.renderer.BatchedEntryRenderer
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer
import me.shedaniel.rei.api.client.gui.widgets.Tooltip
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext
import me.shedaniel.rei.api.common.entry.EntryStack
import moe.nea.firmament.rei.FirmamentReiPlugin.Companion.asItemEntry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.DiffuseLighting
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.json.ModelTransformationMode
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.item.ItemStack

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
        return entry.asItemEntry().getTooltip(tooltipContext, false)
    }

    override fun getExtraData(entry: EntryStack<SBItemStack>): BakedModel {
        return minecraft.itemRenderer.getModel(entry.asItemEntry().value, minecraft.world, minecraft.player, 0)
    }

    override fun getBatchIdentifier(entry: EntryStack<SBItemStack>?, bounds: Rectangle?, extraData: BakedModel): Int {
        return 1738923 + if (extraData.isSideLit) 1 else 0
    }

    override fun startBatch(
        entry: EntryStack<SBItemStack>,
        model: BakedModel,
        graphics: DrawContext,
        delta: Float
    ) {
        setupGL(model)
        val modelViewStack = RenderSystem.getModelViewStack()
        modelViewStack.push()
        modelViewStack.scale(20.0f, -20.0f, 1.0f)
        RenderSystem.applyModelViewMatrix()
    }

    fun setupGL(model: BakedModel) {
        minecraft.textureManager.getTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
            .setFilter(false, false)
        RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
        RenderSystem.enableBlend()
        RenderSystem.blendFunc(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        val sideLit = model.isSideLit
        if (!sideLit) {
            DiffuseLighting.disableGuiDepthLighting()
        }
    }

    override fun renderBase(
        entry: EntryStack<SBItemStack>,
        model: BakedModel,
        graphics: DrawContext,
        immediate: VertexConsumerProvider.Immediate,
        bounds: Rectangle,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        if (entry.isEmpty) return
        val value = entry.asItemEntry().value
        graphics.matrices.push()
        graphics.matrices.translate(bounds.centerX.toFloat() / 20.0f, bounds!!.centerY.toFloat() / -20.0f, 0.0f)
        graphics.matrices.scale(
            bounds.getWidth().toFloat() / 20.0f,
            (bounds.getWidth() + bounds.getHeight()).toFloat() / 2.0f / 20.0f,
            1.0f
        )
        minecraft
            .itemRenderer
            .renderItem(
                value,
                ModelTransformationMode.GUI,
                false,
                graphics.matrices,
                immediate,
                15728880,
                OverlayTexture.DEFAULT_UV,
                model
            )
        graphics.matrices.pop()

    }

    override fun afterBase(
        entry: EntryStack<SBItemStack>,
        model: BakedModel,
        graphics: DrawContext,
        delta: Float
    ) {
        this.endGL(model)
        RenderSystem.getModelViewStack().pop()
        RenderSystem.applyModelViewMatrix()
    }

    fun endGL(model: BakedModel) {
        RenderSystem.enableDepthTest()
        val sideLit = model.isSideLit
        if (!sideLit) {
            DiffuseLighting.enableGuiDepthLighting()
        }
    }

    override fun renderOverlay(
        entry: EntryStack<SBItemStack>,
        extraData: BakedModel,
        graphics: DrawContext,
        immediate: VertexConsumerProvider.Immediate,
        bounds: Rectangle,
        mouseX: Int,
        mouseY: Int,
        delta: Float
    ) {
        val modelViewStack = RenderSystem.getModelViewStack()
        modelViewStack.push()
        modelViewStack.multiplyPositionMatrix(graphics.matrices.peek().positionMatrix)
        modelViewStack.translate(bounds.x.toFloat(), bounds.y.toFloat(), 0.0f)
        modelViewStack.scale(
            bounds.width.toFloat() / 16.0f,
            (bounds.getWidth() + bounds.getHeight()).toFloat() / 2.0f / 16.0f,
            1.0f
        )
        RenderSystem.applyModelViewMatrix()
        renderOverlay(DrawContext(minecraft, graphics.vertexConsumers), entry.asItemEntry())
        modelViewStack.pop()
        RenderSystem.applyModelViewMatrix()
    }
    fun renderOverlay(graphics: DrawContext, entry: EntryStack<ItemStack>) {
        if (!entry.isEmpty) {
            graphics.drawItemInSlot(MinecraftClient.getInstance().textRenderer, entry.value, 0, 0, null)
        }
    }

    override fun endBatch(
        entry: EntryStack<SBItemStack>?,
        extraData: BakedModel?,
        graphics: DrawContext?,
        delta: Float
    ) {
    }

}
