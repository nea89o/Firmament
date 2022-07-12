package moe.nea.notenoughupdates.rei

import com.mojang.blaze3d.vertex.PoseStack
import io.github.moulberry.repo.NEURepository
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
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantments
import java.nio.file.Path
import java.util.stream.Stream


class NEUReiPlugin : REIClientPlugin {

    data class SBItem(val sbname: String, val backing: Item)
    companion object {

        fun EntryStack<NEUReiPlugin.SBItem>.asItemStack() =
            EntryStack.of(VanillaEntryTypes.ITEM, ItemStack(this.value.backing).also {
                it.enchant(Enchantments.BINDING_CURSE, 1)
                it.hoverName = TextComponent(value.sbname)
            })

        val hehe = ResourceLocation("notenoughupdates", "skyblockitems")
    }

    object SBItemEntryDefinition : EntryDefinition<SBItem> {
        override fun equals(o1: SBItem?, o2: SBItem?, context: ComparisonContext?): Boolean {
            return o1 == o2
        }

        override fun getValueType(): Class<SBItem> = SBItem::class.java
        override fun getType(): EntryType<SBItem> =
            EntryType.deferred(hehe)

        override fun getRenderer(): EntryRenderer<SBItem> = object : EntryRenderer<SBItem> {
            override fun render(
                entry: EntryStack<SBItem>,
                matrices: PoseStack,
                bounds: Rectangle,
                mouseX: Int,
                mouseY: Int,
                delta: Float
            ) {
                VanillaEntryTypes.ITEM.definition.renderer
                    .render(
                        entry.asItemStack(),
                        matrices, bounds, mouseX, mouseY, delta
                    )
            }

            override fun getTooltip(entry: EntryStack<SBItem>, mouse: Point): Tooltip? {
                return VanillaEntryTypes.ITEM.definition.renderer
                    .getTooltip(entry.asItemStack(), mouse)
            }

        }

        override fun getSerializer(): EntrySerializer<SBItem>? {
            return null
        }

        override fun getTagsFor(entry: EntryStack<SBItem>?, value: SBItem?): Stream<out TagKey<*>> {
            return Stream.empty()
        }

        override fun asFormattedText(entry: EntryStack<SBItem>, value: SBItem): Component {
            return VanillaEntryTypes.ITEM.definition.asFormattedText(entry.asItemStack(), ItemStack(value.backing))
        }

        override fun hash(entry: EntryStack<SBItem>, value: SBItem, context: ComparisonContext): Long {
            return value.sbname.hashCode().toLong()
        }

        override fun wildcard(entry: EntryStack<SBItem>, value: SBItem): SBItem {
            return value
        }

        override fun normalize(entry: EntryStack<SBItem>, value: SBItem): SBItem {
            return value
        }

        override fun copy(entry: EntryStack<SBItem>?, value: SBItem): SBItem {
            return value.copy()
        }

        override fun isEmpty(entry: EntryStack<SBItem>?, value: SBItem?): Boolean {
            return false
        }

        override fun getIdentifier(entry: EntryStack<SBItem>?, value: SBItem): ResourceLocation? {
            return ResourceLocation("skyblockitem", value.sbname)
        }


    }

    val neuRepo = NEURepository.of(Path.of("NotEnoughUpdates-REPO")).also {
        it.reload()
    }

    override fun registerEntryTypes(registry: EntryTypeRegistry) {
        registry.register(hehe, SBItemEntryDefinition)
    }

    override fun registerEntries(registry: EntryRegistry) {
        neuRepo.items.items.values.forEach {
            println("Adding item: $it")
            registry.addEntry(
                EntryStack.of(
                    SBItemEntryDefinition, SBItem(
                        it.skyblockItemId.lowercase().replace(";", "__"), Registry.ITEM.get(ResourceLocation(it.minecraftItemId))
                    )
                )
            )
        }
        registry.addEntry(EntryStacks.of(ItemStack(Items.DIAMOND).also {
            it.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 10)
        }))
    }
}
