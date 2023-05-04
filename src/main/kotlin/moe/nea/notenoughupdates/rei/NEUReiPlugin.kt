package moe.nea.notenoughupdates.rei

import dev.architectury.event.CompoundEventResult
import io.github.moulberry.repo.data.NEUItem
import me.shedaniel.math.Point
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry
import me.shedaniel.rei.api.client.registry.screen.FocusedStackProvider
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import moe.nea.notenoughupdates.mixins.accessor.AccessorHandledScreen
import moe.nea.notenoughupdates.repo.ItemCache.asItemStack
import moe.nea.notenoughupdates.repo.RepoManager
import moe.nea.notenoughupdates.util.skyBlockId


class NEUReiPlugin : REIClientPlugin {

    companion object {
        fun EntryStack<NEUItem>.asItemEntry(): EntryStack<ItemStack> {
            return EntryStack.of(VanillaEntryTypes.ITEM, value.asItemStack())
        }

        val SKYBLOCK_ITEM_TYPE_ID = Identifier("notenoughupdates", "skyblockitems")
    }

    override fun registerEntryTypes(registry: EntryTypeRegistry) {
        registry.register(SKYBLOCK_ITEM_TYPE_ID, SBItemEntryDefinition)
    }

    override fun registerScreens(registry: ScreenRegistry) {
        registry.registerFocusedStack(object : FocusedStackProvider {
            override fun provide(screen: Screen?, mouse: Point?): CompoundEventResult<EntryStack<*>> {
                if (screen !is HandledScreen<*>) return CompoundEventResult.pass()
                screen as AccessorHandledScreen
                val focusedSlot = screen.focusedSlot_NEU ?: return CompoundEventResult.pass()
                val item = focusedSlot.stack ?: return CompoundEventResult.pass()
                val skyblockId = item.skyBlockId ?: return CompoundEventResult.pass()
                val neuItem = RepoManager.getNEUItem(skyblockId) ?: return CompoundEventResult.interrupt(false, null)
                return CompoundEventResult.interruptTrue(EntryStack.of(SBItemEntryDefinition, neuItem))
            }

            override fun getPriority(): Double = 1_000_000.0
        })
    }

    override fun registerEntries(registry: EntryRegistry) {
        RepoManager.neuRepo.items?.items?.values?.forEach {
            if (!it.isVanilla)
                registry.addEntry(EntryStack.of(SBItemEntryDefinition, it))
        }
    }
}
