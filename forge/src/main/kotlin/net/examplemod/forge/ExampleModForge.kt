package net.examplemod.forge

import dev.architectury.platform.forge.EventBuses
import moe.nea.notenoughupdates.NotEnoughUpdates
import moe.nea.notenoughupdates.NotEnoughUpdates.init
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext

@Mod(NotEnoughUpdates.MOD_ID)
class ExampleModForge {
    init {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(NotEnoughUpdates.MOD_ID, FMLJavaModLoadingContext.get().modEventBus)
        init()
    }
}
