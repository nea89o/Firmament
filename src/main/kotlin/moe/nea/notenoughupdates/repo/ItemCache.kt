package moe.nea.notenoughupdates.repo

import com.mojang.serialization.Dynamic
import io.github.cottonmc.cotton.gui.client.CottonHud
import io.github.moulberry.repo.IReloadable
import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.data.NEUItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import moe.nea.notenoughupdates.NotEnoughUpdates
import moe.nea.notenoughupdates.util.LegacyTagParser
import moe.nea.notenoughupdates.util.appendLore
import net.minecraft.datafixer.Schemas
import net.minecraft.datafixer.TypeReferences
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.concurrent.ConcurrentHashMap

object ItemCache : IReloadable {
    val cache: MutableMap<String, ItemStack> = ConcurrentHashMap()
    val df = Schemas.getFixer()
    var isFlawless = true

    private fun NEUItem.get10809CompoundTag(): NbtCompound = NbtCompound().apply {
        put("tag", LegacyTagParser.parse(nbttag))
        putString("id", minecraftItemId)
        putByte("Count", 1)
        putShort("Damage", damage.toShort())
    }

    private fun NbtCompound.transformFrom10809ToModern(): NbtCompound? =
        try {
            df.update(
                TypeReferences.ITEM_STACK,
                Dynamic(NbtOps.INSTANCE, this),
                -1,
                2975
            ).value as NbtCompound
        } catch (e: Exception) {
            NotEnoughUpdates.logger.error("Failed to datafixer an item", e)
            isFlawless = false
            null
        }

    private fun NEUItem.asItemStackNow(): ItemStack {
        val oldItemTag = get10809CompoundTag()
        val modernItemTag = oldItemTag.transformFrom10809ToModern()
            ?: return ItemStack(Items.PAINTING).apply {
                setCustomName(Text.literal(this@asItemStackNow.displayName))
                appendLore(listOf(Text.literal("Exception rendering item: $skyblockItemId")))
            }
        val itemInstance = ItemStack.fromNbt(modernItemTag)
        if (itemInstance.nbt?.contains("Enchantments") == true) {
            itemInstance.enchantments.add(NbtCompound())
        }
        return itemInstance
    }

    fun NEUItem.asItemStack(): ItemStack {
        var s = cache[this.skyblockItemId]
        if (s == null) {
            s = asItemStackNow()
            cache[this.skyblockItemId] = s
        }
        return s
    }

    fun NEUItem.getIdentifier() =
        Identifier("skyblockitem", skyblockItemId.lowercase().replace(";", "__"))


    var job: Job? = null

    override fun reload(repository: NEURepository) {
        cache.clear()
        val j = job
        if (j != null && j.isActive) {
            j.cancel()
        }

        job = NotEnoughUpdates.coroutineScope.launch {
            val items = repository.items?.items
            if (items == null) {
                CottonHud.remove(RepoManager.progressBar)
                return@launch
            }
            RepoManager.progressBar.reportProgress("Recache Items", 0, items.size)
            CottonHud.add(RepoManager.progressBar)
            var i = 0
            items.values.forEach {
                it.asItemStack() // Rebuild cache
                RepoManager.progressBar.reportProgress("Recache Items", i++, items.size)
            }
            CottonHud.remove(RepoManager.progressBar)
        }
    }
}
