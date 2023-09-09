/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.inventory

import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.events.IsSlotProtectedEvent
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen
import moe.nea.firmament.util.CommonSoundEffects
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.data.ProfileSpecificDataHolder
import net.minecraft.entity.player.PlayerInventory
import org.lwjgl.glfw.GLFW

object SlotLocking : FirmamentFeature {
    override val identifier: String
        get() = "slot-locking"

    @Serializable
    data class Data(
        val lockedSlots: MutableSet<Int> = mutableSetOf(),
    )

    object TConfig : ManagedConfig(identifier) {
        val lock by keyBinding("lock") { GLFW.GLFW_KEY_L }
    }

    override val config: TConfig
            get() = TConfig

    object DConfig : ProfileSpecificDataHolder<Data>(serializer(), "locked-slots", ::Data)

    val lockedSlots get() = DConfig.data?.lockedSlots
    override fun onLoad() {
        HandledScreenKeyPressedEvent.subscribe {
            if (!it.matches(TConfig.lock)) return@subscribe
            val inventory = MC.handledScreen ?: return@subscribe
            inventory as AccessorHandledScreen

            val slot = inventory.focusedSlot_Firmament ?: return@subscribe
            val lockedSlots = lockedSlots ?: return@subscribe
            if (slot.inventory is PlayerInventory) {
                if (slot.index in lockedSlots) {
                    lockedSlots.remove(slot.index)
                } else {
                    lockedSlots.add(slot.index)
                }
                DConfig.markDirty()
                CommonSoundEffects.playSuccess()
            }
        }
        IsSlotProtectedEvent.subscribe {
            if (it.slot.inventory is PlayerInventory && it.slot.index in (lockedSlots ?: setOf())) {
                it.protect()
            }
        }
        SlotRenderEvents.Before.subscribe {
            if (it.slot.inventory is PlayerInventory && it.slot.index in (lockedSlots ?: setOf())) {
                it.context.fill(
                    it.slot.x,
                    it.slot.y,
                    it.slot.x + 16,
                    it.slot.y + 16,
                    0xFFFF0000.toInt()
                )
            }
        }
    }
}
