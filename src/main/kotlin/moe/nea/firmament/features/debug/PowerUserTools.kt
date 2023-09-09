/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.debug

import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import moe.nea.firmament.events.CustomItemModelEvent
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.events.ItemTooltipEvent
import moe.nea.firmament.events.ScreenOpenEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen
import moe.nea.firmament.util.ClipboardUtils
import moe.nea.firmament.util.skyBlockId

object PowerUserTools : FirmamentFeature {
    override val identifier: String
        get() = "power-user"

    object TConfig : ManagedConfig(identifier) {
        val showItemIds by toggle("show-item-id") { false }
        val copyItemId by keyBindingWithDefaultUnbound("copy-item-id")
        val copyTexturePackId by keyBindingWithDefaultUnbound("copy-texture-pack-id")
        val copyNbtData by keyBindingWithDefaultUnbound("copy-nbt-data")
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
                it.lines.add(Text.translatable("firmament.tooltip.skyblockid", id.neuItem))
            }
            val (item, text) = lastCopiedStack ?: return@subscribe
            if (item != it.stack) {
                lastCopiedStack = null
                return@subscribe
            }
            lastCopiedStackViewTime = true
            it.lines.add(text)
        }
        TickEvent.subscribe {
            if (!lastCopiedStackViewTime)
                lastCopiedStack = null
            lastCopiedStackViewTime = false
        }
        ScreenOpenEvent.subscribe {
            lastCopiedStack = null
        }
        HandledScreenKeyPressedEvent.subscribe {
            if (it.screen !is AccessorHandledScreen) return@subscribe
            val item = it.screen.focusedSlot_Firmament?.stack ?: return@subscribe
            if (it.matches(TConfig.copyItemId)) {
                val sbId = item.skyBlockId
                if (sbId == null) {
                    lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.skyblockid.fail"))
                    return@subscribe
                }
                ClipboardUtils.setTextContent(sbId.neuItem)
                lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.skyblockid", sbId.neuItem))
            } else if (it.matches(TConfig.copyTexturePackId)) {
                val model = CustomItemModelEvent.getModelIdentifier(item)
                if (model == null) {
                    lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.modelid.fail"))
                    return@subscribe
                }
                ClipboardUtils.setTextContent(model.toString())
                lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.modelid", model.toString()))
            } else if (it.matches(TConfig.copyNbtData)) {
                val nbt = item.orCreateNbt.toString()
                ClipboardUtils.setTextContent(nbt)
                lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.nbt"))
            }
        }
    }


}
