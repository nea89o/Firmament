package net.examplemod.fabric

import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path
import net.examplemod.ExampleExpectPlatform

object ExampleExpectPlatformImpl {
    /**
     * This is our actual method to [ExampleExpectPlatform.getConfigDirectory].
     */
    @JvmStatic
    fun getConfigDirectory(): Path = FabricLoader.getInstance().configDir
}