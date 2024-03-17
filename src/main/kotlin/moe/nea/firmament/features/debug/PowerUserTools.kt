/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.debug

import net.minecraft.block.SkullBlock
import net.minecraft.block.entity.SkullBlockEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtHelper
import net.minecraft.text.Text
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import moe.nea.firmament.events.CustomItemModelEvent
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.events.ItemTooltipEvent
import moe.nea.firmament.events.ScreenChangeEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.events.WorldKeyboardEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.features.texturepack.CustomSkyBlockTextures
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen
import moe.nea.firmament.util.ClipboardUtils
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.focusedItemStack
import moe.nea.firmament.util.getOrCreateCompoundTag
import moe.nea.firmament.util.skyBlockId

object PowerUserTools : FirmamentFeature {
    override val identifier: String
        get() = "power-user"

    object TConfig : ManagedConfig(identifier) {
        val showItemIds by toggle("show-item-id") { false }
        val copyItemId by keyBindingWithDefaultUnbound("copy-item-id")
        val copyTexturePackId by keyBindingWithDefaultUnbound("copy-texture-pack-id")
        val copyNbtData by keyBindingWithDefaultUnbound("copy-nbt-data")
        val copySkullTexture by keyBindingWithDefaultUnbound("copy-skull-texture")
    }

    override val config
        get() = TConfig

    var lastCopiedStack: Pair<ItemStack, Text>? = null
        set(value) {
            field = value
            if (value != null)
                lastCopiedStackViewTime = true
        }
    var lastCopiedStackViewTime = false

    override fun onLoad() {
        ItemTooltipEvent.subscribe {
            if (TConfig.showItemIds) {
                val id = it.stack.skyBlockId ?: return@subscribe
                it.lines.add(Text.stringifiedTranslatable("firmament.tooltip.skyblockid", id.neuItem))
            }
            val (item, text) = lastCopiedStack ?: return@subscribe
            if (!ItemStack.areEqual(item, it.stack)) {
                lastCopiedStack = null
                return@subscribe
            }
            lastCopiedStackViewTime = true
            it.lines.add(text)
        }
        WorldKeyboardEvent.subscribe {
            if (it.matches(TConfig.copySkullTexture)) {
                val p = MC.camera ?: return@subscribe
                val blockHit = p.raycast(20.0, 0.0f, false) ?: return@subscribe
                if (blockHit.type != HitResult.Type.BLOCK || blockHit !is BlockHitResult) {
                    MC.sendChat(Text.translatable("firmament.tooltip.copied.skull.fail"))
                    return@subscribe
                }
                val blockAt = p.world.getBlockState(blockHit.blockPos)?.block
                val entity = p.world.getBlockEntity(blockHit.blockPos)
                if (blockAt !is SkullBlock || entity !is SkullBlockEntity || entity.owner == null) {
                    MC.sendChat(Text.translatable("firmament.tooltip.copied.skull.fail"))
                    return@subscribe
                }
                val id = CustomSkyBlockTextures.getSkullTexture(entity.owner!!)
                if (id == null) {
                    MC.sendChat(Text.translatable("firmament.tooltip.copied.skull.fail"))
                } else {
                    ClipboardUtils.setTextContent(id.toString())
                    MC.sendChat(Text.stringifiedTranslatable("firmament.tooltip.copied.skull", id.toString()))
                }
            }
        }
        TickEvent.subscribe {
            if (!lastCopiedStackViewTime)
                lastCopiedStack = null
            lastCopiedStackViewTime = false
        }
        ScreenChangeEvent.subscribe {
            lastCopiedStack = null
        }
        HandledScreenKeyPressedEvent.subscribe {
            if (it.screen !is AccessorHandledScreen) return@subscribe
            val item = it.screen.focusedItemStack ?: return@subscribe
            if (it.matches(TConfig.copyItemId)) {
                val sbId = item.skyBlockId
                if (sbId == null) {
                    lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.skyblockid.fail"))
                    return@subscribe
                }
                ClipboardUtils.setTextContent(sbId.neuItem)
                lastCopiedStack =
                    Pair(item, Text.stringifiedTranslatable("firmament.tooltip.copied.skyblockid", sbId.neuItem))
            } else if (it.matches(TConfig.copyTexturePackId)) {
                val model = CustomItemModelEvent.getModelIdentifier(item)
                if (model == null) {
                    lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.modelid.fail"))
                    return@subscribe
                }
                ClipboardUtils.setTextContent(model.toString())
                lastCopiedStack =
                    Pair(item, Text.stringifiedTranslatable("firmament.tooltip.copied.modelid", model.toString()))
            } else if (it.matches(TConfig.copyNbtData)) {
                val nbt = item.orCreateNbt.toString()
                ClipboardUtils.setTextContent(nbt)
                lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.nbt"))
            } else if (it.matches(TConfig.copySkullTexture)) {
                if (item.item != Items.PLAYER_HEAD) {
                    lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.skull-id.fail.no-skull"))
                    return@subscribe
                }
                val profile = NbtHelper.toGameProfile(item.orCreateNbt.getOrCreateCompoundTag("SkullOwner"))
                if (profile == null) {
                    lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.skull-id.fail.no-profile"))
                    return@subscribe
                }
                val skullTexture = CustomSkyBlockTextures.getSkullTexture(profile)
                if (skullTexture == null) {
                    lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.skull-id.fail.no-texture"))
                    return@subscribe
                }
                ClipboardUtils.setTextContent(skullTexture.toString())
                lastCopiedStack =
                    Pair(item, Text.stringifiedTranslatable("firmament.tooltip.copied.skull-id", skullTexture.toString()))
                println("Copied skull id: $skullTexture")
            }
        }
    }


}
