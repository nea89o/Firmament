package net.examplemod.fabric

import org.quiltmc.loader.api.QuiltLoader
import java.nio.file.Path
import net.examplemod.ExampleExpectPlatform

object ExampleExpectPlatformImpl {
    /**
     * This is our actual method to [ExampleExpectPlatform.getConfigDirectory].
     */
    @JvmStatic
    fun getConfigDirectory(): Path = QuiltLoader.getConfigDir()
}