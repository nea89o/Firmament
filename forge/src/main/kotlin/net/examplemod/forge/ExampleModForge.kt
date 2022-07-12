package net.examplemod.forge

import dev.architectury.platform.forge.EventBuses
import net.examplemod.ExampleMod
import net.examplemod.ExampleMod.init
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext

@Mod(ExampleMod.MOD_ID)
class ExampleModForge {
    init {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(ExampleMod.MOD_ID, FMLJavaModLoadingContext.get().modEventBus)
        init()
    }
}