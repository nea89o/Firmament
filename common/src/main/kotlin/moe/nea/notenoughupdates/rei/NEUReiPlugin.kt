package moe.nea.notenoughupdates.rei

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.serialization.Dynamic
import io.github.moulberry.repo.data.NEUItem
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer
import me.shedaniel.rei.api.client.gui.widgets.Tooltip
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry
import me.shedaniel.rei.api.common.entry.EntrySerializer
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.entry.comparison.ComparisonContext
import me.shedaniel.rei.api.common.entry.type.EntryDefinition
import me.shedaniel.rei.api.common.entry.type.EntryType
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes
import me.shedaniel.rei.api.common.util.EntryStacks
import moe.nea.notenoughupdates.LegacyTagParser
import moe.nea.notenoughupdates.NotEnoughUpdates.neuRepo
import net.minecraft.ChatFormatting
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.util.datafix.DataFixers.getDataFixer
import net.minecraft.util.datafix.fixes.References
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantments
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Stream


class NEUReiPlugin : REIClientPlugin {

    companion object {

        fun EntryStack<NEUItem>.asItemEntry(): EntryStack<ItemStack> {
            return EntryStack.of(VanillaEntryTypes.ITEM, value.asItemStack())
        }

        fun ItemStack.appendLore(args: List<Component>) {
            val compoundTag = getOrCreateTagElement("display")
            val loreList = compoundTag.getList("Lore", StringTag.TAG_STRING.toInt())
            for (arg in args) {
                loreList.add(StringTag.valueOf(Component.Serializer.toJson(arg)))
            }
            compoundTag.put("Lore", loreList)
        }

        val cache: MutableMap<String, ItemStack> = ConcurrentHashMap()

        fun NEUItem.asItemStackNow(): ItemStack {
            val df = getDataFixer()
            val itemTag1_8_9 = CompoundTag()
            itemTag1_8_9.put("tag", LegacyTagParser.parse(this.nbttag))
            itemTag1_8_9.putString("id", this.minecraftItemId)
            itemTag1_8_9.putByte("Count", 1)
            itemTag1_8_9.putShort("Damage", this.damage.toShort())
            val itemTag_modern = try {
                df.update(
                    References.ITEM_STACK,
                    Dynamic(NbtOps.INSTANCE, itemTag1_8_9),
                    99,
                    2975
                ).value as CompoundTag
            } catch (e: Exception) {
                e.printStackTrace()
                return ItemStack(Items.PAINTING).apply {
                    appendLore(listOf(TextComponent("Exception rendering item: $skyblockItemId")))
                }
            }
            val itemInstance = ItemStack.of(itemTag_modern)
            return itemInstance.also {
                if(false)it.appendLore(
                    listOf(
                        TextComponent("Old: $minecraftItemId").withStyle {
                            it.withItalic(false).withColor(ChatFormatting.RED)
                        },
                        TextComponent("Modern: $itemTag_modern").withStyle {
                            it.withItalic(false).withColor(ChatFormatting.RED)
                        },
                    )
                )
                it.hoverName = TextComponent(this.skyblockItemId)
            }
        }

        fun NEUItem.asItemStack(): ItemStack {
            var s = cache[this.skyblockItemId]
            if (s == null) {
                s = asItemStackNow()
                cache[this.skyblockItemId] = s
            }
            return s
        }


        val hehe = ResourceLocation("notenoughupdates", "skyblockitems")
    }

    object SBItemEntryDefinition : EntryDefinition<NEUItem> {
        override fun equals(o1: NEUItem?, o2: NEUItem?, context: ComparisonContext?): Boolean {
            return o1 == o2
        }

        override fun getValueType(): Class<NEUItem> = NEUItem::class.java
        override fun getType(): EntryType<NEUItem> =
            EntryType.deferred(hehe)

        override fun getRenderer(): EntryRenderer<NEUItem> = object : EntryRenderer<NEUItem> {
            override fun render(
                entry: EntryStack<NEUItem>,
                matrices: PoseStack,
                bounds: Rectangle,
                mouseX: Int,
                mouseY: Int,
                delta: Float
            ) {
                VanillaEntryTypes.ITEM.definition.renderer
                    .render(
                        entry.asItemEntry(),
                        matrices, bounds, mouseX, mouseY, delta
                    )
            }

            override fun getTooltip(entry: EntryStack<NEUItem>, mouse: Point): Tooltip? {
                return VanillaEntryTypes.ITEM.definition.renderer
                    .getTooltip(entry.asItemEntry(), mouse)
            }

        }

        override fun getSerializer(): EntrySerializer<NEUItem>? {
            return null
        }

        override fun getTagsFor(entry: EntryStack<NEUItem>?, value: NEUItem?): Stream<out TagKey<*>> {
            return Stream.empty()
        }

        override fun asFormattedText(entry: EntryStack<NEUItem>, value: NEUItem): Component {
            return VanillaEntryTypes.ITEM.definition.asFormattedText(entry.asItemEntry(), value.asItemStack())
        }

        override fun hash(entry: EntryStack<NEUItem>, value: NEUItem, context: ComparisonContext): Long {
            return value.skyblockItemId.hashCode().toLong()
        }

        override fun wildcard(entry: EntryStack<NEUItem>, value: NEUItem): NEUItem {
            return value
        }

        override fun normalize(entry: EntryStack<NEUItem>, value: NEUItem): NEUItem {
            return value
        }

        override fun copy(entry: EntryStack<NEUItem>?, value: NEUItem): NEUItem {
            return value
        }

        override fun isEmpty(entry: EntryStack<NEUItem>?, value: NEUItem?): Boolean {
            return false
        }

        override fun getIdentifier(entry: EntryStack<NEUItem>?, value: NEUItem): ResourceLocation {
            return ResourceLocation("skyblockitem", value.skyblockItemId.lowercase().replace(";", "__"))
        }


    }

    override fun registerEntryTypes(registry: EntryTypeRegistry) {
        registry.register(hehe, SBItemEntryDefinition)
    }

    override fun registerEntries(registry: EntryRegistry) {
        neuRepo.items.items.values.forEach {
            registry.addEntry(EntryStack.of(SBItemEntryDefinition, it))
        }
        registry.addEntry(EntryStacks.of(ItemStack(Items.DIAMOND).also {
            it.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 10)
        }))
    }
}
