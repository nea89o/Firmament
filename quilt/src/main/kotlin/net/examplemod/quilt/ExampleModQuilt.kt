package net.examplemod.quilt

import net.examplemod.ExampleMod.init
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer

@Suppress("unused")
class ExampleModQuilt : ModInitializer {
    override fun onInitialize(mod: ModContainer) {
        init()
    }
}