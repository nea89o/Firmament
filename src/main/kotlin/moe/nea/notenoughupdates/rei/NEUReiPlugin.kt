package moe.nea.notenoughupdates.rei

import io.github.moulberry.repo.data.NEUItem
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import moe.nea.notenoughupdates.repo.ItemCache.asItemStack
import moe.nea.notenoughupdates.repo.RepoManager
import moe.nea.notenoughupdates.util.SkyblockId


class NEUReiPlugin : REIClientPlugin {

    companion object {
        fun EntryStack<NEUItem>.asItemEntry(): EntryStack<ItemStack> {
            return EntryStack.of(VanillaEntryTypes.ITEM, value?.asItemStack())
        }

        val SKYBLOCK_ITEM_TYPE_ID = Identifier("notenoughupdates", "skyblockitems")
    }

    override fun registerEntryTypes(registry: EntryTypeRegistry) {
        registry.register(SKYBLOCK_ITEM_TYPE_ID, SBItemEntryDefinition)
    }

    override fun registerCollapsibleEntries(registry: CollapsibleEntryRegistry) {
        RepoManager.neuRepo.constants.parents.parents
            .forEach { (parent, children) ->
                registry.group(
                    SkyblockId(parent).identifier,
                    Text.literal(RepoManager.getNEUItem(SkyblockId(parent))?.displayName ?: parent),
                    (children + parent).map { SBItemEntryDefinition.getEntry(RepoManager.getNEUItem(SkyblockId(it))) })
            }
    }

    override fun registerScreens(registry: ScreenRegistry) {
        registry.registerFocusedStack(SkyblockItemIdFocusedStackProvider)
    }

    override fun registerEntries(registry: EntryRegistry) {
        RepoManager.neuRepo.items?.items?.values?.forEach {
            if (!it.isVanilla)
                registry.addEntry(EntryStack.of(SBItemEntryDefinition, it))
        }
    }
}
