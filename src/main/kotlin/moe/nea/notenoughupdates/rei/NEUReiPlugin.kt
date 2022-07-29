package moe.nea.notenoughupdates.rei

import io.github.moulberry.repo.data.NEUItem
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes
import moe.nea.notenoughupdates.NotEnoughUpdates.neuRepo
import moe.nea.notenoughupdates.repo.ItemCache.asItemStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack


class NEUReiPlugin : REIClientPlugin {

    companion object {

        fun EntryStack<NEUItem>.asItemEntry(): EntryStack<ItemStack> {
            return EntryStack.of(VanillaEntryTypes.ITEM, value.asItemStack())
        }

        val SKYBLOCK_ITEM_TYPE_ID = ResourceLocation("notenoughupdates", "skyblockitems")
    }
    override fun registerEntryTypes(registry: EntryTypeRegistry) {
        registry.register(SKYBLOCK_ITEM_TYPE_ID, SBItemEntryDefinition)
    }


    override fun registerEntries(registry: EntryRegistry) {
        neuRepo.items.items.values.forEach {
            if (!it.isVanilla)
                registry.addEntry(EntryStack.of(SBItemEntryDefinition, it))
        }
    }
}
