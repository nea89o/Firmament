package moe.nea.notenoughupdates.repo

import com.mojang.serialization.Dynamic
import io.github.moulberry.repo.IReloadable
import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.data.NEUItem
import moe.nea.notenoughupdates.util.LegacyTagParser
import moe.nea.notenoughupdates.util.appendLore
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.TextComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.datafix.DataFixers
import net.minecraft.util.datafix.fixes.References
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import java.util.concurrent.ConcurrentHashMap

object ItemCache : IReloadable {
    val cache: MutableMap<String, ItemStack> = ConcurrentHashMap()
    val df = DataFixers.getDataFixer()
    var isFlawless = true

    private fun NEUItem.get10809CompoundTag(): CompoundTag = CompoundTag().apply {
        put("tag", LegacyTagParser.parse(nbttag))
        putString("id", minecraftItemId)
        putByte("Count", 1)
        putShort("Damage", damage.toShort())
    }

    private fun CompoundTag.transformFrom10809ToModern(): CompoundTag? =
        try {
            df.update(
                References.ITEM_STACK,
                Dynamic(NbtOps.INSTANCE, this),
                -1,
                2975
            ).value as CompoundTag
        } catch (e: Exception) {
            e.printStackTrace()
            isFlawless = false
            null
        }

    private fun NEUItem.asItemStackNow(): ItemStack {
        val oldItemTag = get10809CompoundTag()
        val modernItemTag = oldItemTag.transformFrom10809ToModern()
            ?: return ItemStack(Items.PAINTING).apply {
                setHoverName(TextComponent(this@asItemStackNow.displayName))
                appendLore(listOf(TextComponent("Exception rendering item: $skyblockItemId")))
            }
        val itemInstance = ItemStack.of(modernItemTag)
        if (itemInstance.tag?.contains("Enchantments") == true) {
            itemInstance.enchantmentTags.add(CompoundTag())
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

    fun NEUItem.getResourceLocation() =
        ResourceLocation("skyblockitem", skyblockItemId.lowercase().replace(";", "__"))



    override fun reload(repository: NEURepository) {
        cache.clear()
    }
}
