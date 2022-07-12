package net.examplemod.fabric

import net.examplemod.ExampleMod.init
import net.fabricmc.api.ModInitializer

class ExampleModFabric : ModInitializer {
    override fun onInitialize() {
        init()
    }
}