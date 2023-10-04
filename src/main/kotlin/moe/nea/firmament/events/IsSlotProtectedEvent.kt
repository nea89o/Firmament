/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import moe.nea.firmament.util.CommonSoundEffects
import moe.nea.firmament.util.MC

data class IsSlotProtectedEvent(
    val slot: Slot?,
    val actionType: SlotActionType,
    var isProtected: Boolean,
    val itemStackOverride: ItemStack?,
    var silent: Boolean = false,
) : FirmamentEvent() {
    val itemStack get() = itemStackOverride ?: slot!!.stack

    fun protect() {
        isProtected = true
    }

    fun protectSilent() {
        if (!isProtected) {
            silent = true
        }
        isProtected = true
    }

    companion object : FirmamentEventBus<IsSlotProtectedEvent>() {
        @JvmStatic
        @JvmOverloads
        fun shouldBlockInteraction(slot: Slot?, action: SlotActionType, itemStackOverride: ItemStack? = null): Boolean {
            if (slot == null && itemStackOverride == null) return false
            val event = IsSlotProtectedEvent(slot, action, false, itemStackOverride)
            publish(event)
            if (event.isProtected && !event.silent) {
                MC.player?.sendMessage(Text.translatable("firmament.protectitem").append(event.itemStack.name))
                CommonSoundEffects.playFailure()
            }
            return event.isProtected
        }
    }
}
