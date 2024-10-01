

@file:UseSerializers(DashlessUUIDSerializer::class)

package moe.nea.firmament.features.inventory

import com.mojang.blaze3d.systems.RenderSystem
import java.util.UUID
import org.lwjgl.glfw.GLFW
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.serializer
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.Identifier
import moe.nea.firmament.annotations.Subscribe
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
import moe.nea.firmament.util.SkyBlockIsland
import moe.nea.firmament.util.data.ProfileSpecificDataHolder
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt
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
            SkyBlockIsland.RIFT -> DConfig.data?.lockedSlotsRift
            null -> null
            else -> DConfig.data?.lockedSlots
        }

    fun isSalvageScreen(screen: HandledScreen<*>?): Boolean {
        if (screen == null) return false
        return screen.title.unformattedString.contains("Salvage Item")
    }

    fun isTradeScreen(screen: HandledScreen<*>?): Boolean {
        if (screen == null) return false
        val handler = screen.screenHandler as? GenericContainerScreenHandler ?: return false
        if (handler.inventory.size() < 9) return false
        val middlePane = handler.inventory.getStack(handler.inventory.size() - 5)
        if (middlePane == null) return false
        return middlePane.displayNameAccordingToNbt?.unformattedString == "⇦ Your stuff"
    }

    fun isNpcShop(screen: HandledScreen<*>?): Boolean {
        if (screen == null) return false
        val handler = screen.screenHandler as? GenericContainerScreenHandler ?: return false
        if (handler.inventory.size() < 9) return false
        val sellItem = handler.inventory.getStack(handler.inventory.size() - 5)
        if (sellItem == null) return false
        if (sellItem.displayNameAccordingToNbt?.unformattedString == "Sell Item") return true
        val lore = sellItem.loreAccordingToNbt
        return (lore.lastOrNull() ?: return false).unformattedString == "Click to buyback!"
    }

    @Subscribe
    fun onSalvageProtect(event: IsSlotProtectedEvent) {
        if (event.slot == null) return
        if (!event.slot.hasStack()) return
        if (event.slot.stack.displayNameAccordingToNbt?.unformattedString != "Salvage Items") return
        val inv = event.slot.inventory
        var anyBlocked = false
        for (i in 0 until event.slot.index) {
            val stack = inv.getStack(i)
            if (IsSlotProtectedEvent.shouldBlockInteraction(null, SlotActionType.THROW, stack))
                anyBlocked = true
        }
        if (anyBlocked) {
            event.protectSilent()
        }
    }

    @Subscribe
    fun onProtectUuidItems(event: IsSlotProtectedEvent) {
        val doesNotDeleteItem = event.actionType == SlotActionType.SWAP
            || event.actionType == SlotActionType.PICKUP
            || event.actionType == SlotActionType.QUICK_MOVE
            || event.actionType == SlotActionType.QUICK_CRAFT
            || event.actionType == SlotActionType.CLONE
            || event.actionType == SlotActionType.PICKUP_ALL
        val isSellOrTradeScreen =
            isNpcShop(MC.handledScreen) || isTradeScreen(MC.handledScreen) || isSalvageScreen(MC.handledScreen)
        if ((!isSellOrTradeScreen || event.slot?.inventory !is PlayerInventory)
            && doesNotDeleteItem
        ) return
        val stack = event.itemStack ?: return
        val uuid = stack.skyblockUUID ?: return
        if (uuid in (lockedUUIDs ?: return)) {
            event.protect()
        }
    }

    @Subscribe
    fun onProtectSlot(it: IsSlotProtectedEvent) {
        if (it.slot != null && it.slot.inventory is PlayerInventory && it.slot.index in (lockedSlots ?: setOf())) {
            it.protect()
        }
    }

    @Subscribe
    fun onLockUUID(it: HandledScreenKeyPressedEvent) {
        if (!it.matches(TConfig.lockUUID)) return
        val inventory = MC.handledScreen ?: return
        inventory as AccessorHandledScreen

        val slot = inventory.focusedSlot_Firmament ?: return
        val stack = slot.stack ?: return
        val uuid = stack.skyblockUUID ?: return
        val lockedUUIDs = lockedUUIDs ?: return
        if (uuid in lockedUUIDs) {
            lockedUUIDs.remove(uuid)
        } else {
            lockedUUIDs.add(uuid)
        }
        DConfig.markDirty()
        CommonSoundEffects.playSuccess()
        it.cancel()
    }

    @Subscribe
    fun onLockSlot(it: HandledScreenKeyPressedEvent) {
        if (!it.matches(TConfig.lockSlot)) return
        val inventory = MC.handledScreen ?: return
        inventory as AccessorHandledScreen

        val slot = inventory.focusedSlot_Firmament ?: return
        val lockedSlots = lockedSlots ?: return
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

    @Subscribe
    fun onRenderSlotOverlay(it: SlotRenderEvents.After) {
        val isSlotLocked = it.slot.inventory is PlayerInventory && it.slot.index in (lockedSlots ?: setOf())
        val isUUIDLocked = (it.slot.stack?.skyblockUUID) in (lockedUUIDs ?: setOf())
        if (isSlotLocked || isUUIDLocked) {
            RenderSystem.disableDepthTest()
            it.context.drawSprite(
                it.slot.x, it.slot.y, 0,
                16, 16,
                MC.guiAtlasManager.getSprite(
                    when {
                        isSlotLocked ->
                            (Identifier.of("firmament:slot_locked"))

                        isUUIDLocked ->
                            (Identifier.of("firmament:uuid_locked"))

                        else ->
                            error("unreachable")
                    }
                )
            )
            RenderSystem.enableDepthTest()
        }
    }
}
