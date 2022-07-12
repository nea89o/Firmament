package net.examplemod

import com.google.common.base.Suppliers
import dev.architectury.registry.CreativeTabRegistry
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.Registries
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.util.function.Supplier

object ExampleMod {
    const val MOD_ID = "examplemod"

    // We can use this if we don't want to use DeferredRegister
    @Suppress("unused")
    val REGISTRIES: Supplier<Registries> = Suppliers.memoize { Registries.get(MOD_ID) }

    // Registering a new creative tab
    val EXAMPLE_TAB: CreativeModeTab = CreativeTabRegistry.create(ResourceLocation(MOD_ID, "example_tab")) { ItemStack(EXAMPLE_ITEM.get()) }
    val ITEMS: DeferredRegister<Item> = DeferredRegister.create(MOD_ID, Registry.ITEM_REGISTRY)
    val EXAMPLE_ITEM: RegistrySupplier<Item> = ITEMS.register("example_item") { Item(Item.Properties().tab(EXAMPLE_TAB)) }

    fun init() {
        ITEMS.register()
        println(ExampleExpectPlatform.getConfigDirectory().toAbsolutePath().normalize().toString())
    }
}
