/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
@file:UseSerializers(DashlessUUIDSerializer::class)

package moe.nea.firmament.features.inventory

import java.util.*
import org.lwjgl.glfw.GLFW
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.serializer
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.slot.SlotActionType
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.events.IsSlotProtectedEvent
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.keybindings.SavedKeyBinding
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen
import moe.nea.firmament.util.CommonSoundEffects
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.data.ProfileSpecificDataHolder
import moe.nea.firmament.util.item.displayNameAccordingToNbt
import moe.nea.firmament.util.item.loreAccordingToNbt
import moe.nea.firmament.util.json.DashlessUUIDSerializer
import moe.nea.firmament.util.skyblockUUID
import moe.nea.firmament.util.unformattedString

object SlotLocking : FirmamentFeature {
    override val identifier: String
        get() = "slot-locking"

    @Serializable
    data class Data(
        val lockedSlots: MutableSet<Int> = mutableSetOf(),
        val lockedSlotsRift: MutableSet<Int> = mutableSetOf(),

        val lockedUUIDs: MutableSet<UUID> = mutableSetOf(),
    )

    object TConfig : ManagedConfig(identifier) {
        val lockSlot by keyBinding("lock") { GLFW.GLFW_KEY_L }
        val lockUUID by keyBindingWithOutDefaultModifiers("lock-uuid") {
            SavedKeyBinding(GLFW.GLFW_KEY_L, shift = true)
        }
    }

    override val config: TConfig
        get() = TConfig

    object DConfig : ProfileSpecificDataHolder<Data>(serializer(), "locked-slots", ::Data)

    val lockedUUIDs get() = DConfig.data?.lockedUUIDs

    val lockedSlots
        get() = when (SBData.skyblockLocation) {
            "rift" -> DConfig.data?.lockedSlotsRift
            null -> null
            else -> DConfig.data?.lockedSlots
        }

    fun isSalvageScreen(screen: HandledScreen<*>?): Boolean {
        if (screen == null) return false
        return screen.title.unformattedString.contains("Salvage Item")
    }

    fun isTradeScreen(screen: HandledScreen<*>?): Boolean {
        if (screen == null) return false
        val handler = screen.screenHandler
        if (handler.slots.size < 9) return false
        val middlePane = handler.getSlot(4)
        if (!middlePane.hasStack()) return false
        return middlePane.stack.displayNameAccordingToNbt?.unformattedString == "⇦ Your stuff"
    }

    fun isNpcShop(screen: HandledScreen<*>?): Boolean {
        if (screen == null) return false
        val handler = screen.screenHandler
        if (handler.slots.size < 9) return false
        val sellItem = handler.getSlot(handler.slots.size - 5)
        if (!sellItem.hasStack()) return false
        if (sellItem.stack.displayNameAccordingToNbt?.unformattedString == "Sell Item") return true
        val lore = sellItem.stack.loreAccordingToNbt
        return (lore.lastOrNull() ?: return false).value?.unformattedString == "Click to buyback!"
    }

    override fun onLoad() {
        HandledScreenKeyPressedEvent.subscribe {
            if (!it.matches(TConfig.lockSlot)) return@subscribe
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
        HandledScreenKeyPressedEvent.subscribe {
            if (!it.matches(TConfig.lockUUID)) return@subscribe
            val inventory = MC.handledScreen ?: return@subscribe
            inventory as AccessorHandledScreen

            val slot = inventory.focusedSlot_Firmament ?: return@subscribe
            val stack = slot.stack ?: return@subscribe
            val uuid = stack.skyblockUUID ?: return@subscribe
            val lockedUUIDs = lockedUUIDs ?: return@subscribe
            if (uuid in lockedUUIDs) {
                lockedUUIDs.remove(uuid)
            } else {
                lockedUUIDs.add(uuid)
            }
            DConfig.markDirty()
            CommonSoundEffects.playSuccess()
        }
        IsSlotProtectedEvent.subscribe {
            if (it.slot != null && it.slot.inventory is PlayerInventory && it.slot.index in (lockedSlots ?: setOf())) {
                it.protect()
            }
        }
        IsSlotProtectedEvent.subscribe { event ->
            val doesNotDeleteItem = event.actionType == SlotActionType.SWAP
                || event.actionType == SlotActionType.PICKUP
                || event.actionType == SlotActionType.QUICK_MOVE
                || event.actionType == SlotActionType.QUICK_CRAFT
                || event.actionType == SlotActionType.CLONE
                || event.actionType == SlotActionType.PICKUP_ALL
            val isSellOrTradeScreen =
                isNpcShop(MC.handledScreen) || isTradeScreen(MC.handledScreen) || isSalvageScreen(MC.handledScreen)
            if (!isSellOrTradeScreen && doesNotDeleteItem) return@subscribe
            val stack = event.itemStack ?: return@subscribe
            val uuid = stack.skyblockUUID ?: return@subscribe
            if (uuid in (lockedUUIDs ?: return@subscribe)) {
                event.protect()
            }
        }
        IsSlotProtectedEvent.subscribe { event ->
            if (event.slot == null) return@subscribe
            if (!event.slot.hasStack()) return@subscribe
            if (event.slot.stack.displayNameAccordingToNbt?.unformattedString != "Salvage Items") return@subscribe
            val inv = event.slot.inventory
            var anyBlocked = false
            for (i in 0 until event.slot.index) {
                val stack = inv.getStack(i)
                if (IsSlotProtectedEvent.shouldBlockInteraction(null, SlotActionType.THROW, stack))
                    anyBlocked = true
            }
            if(anyBlocked) {
                event.protectSilent()
            }
        }
        SlotRenderEvents.Before.subscribe {
            val isSlotLocked = it.slot.inventory is PlayerInventory && it.slot.index in (lockedSlots ?: setOf())
            val isUUIDLocked = (it.slot.stack?.skyblockUUID) in (lockedUUIDs ?: setOf())
            if (isSlotLocked || isUUIDLocked) {
                it.context.fill(
                    it.slot.x,
                    it.slot.y,
                    it.slot.x + 16,
                    it.slot.y + 16,
                    when {
                        isSlotLocked -> 0xFFFF0000.toInt()
                        isUUIDLocked -> 0xFF00FF00.toInt()
                        else -> error("Slot is locked, but not by slot or uuid")
                    }
                )
            }
        }
    }
}
