package net.examplemod.forge

import net.minecraftforge.fml.loading.FMLPaths
import java.nio.file.Path
import net.examplemod.ExampleExpectPlatform

@Suppress("unused")
object ExampleExpectPlatformImpl {
    /**
     * This is our actual method to [ExampleExpectPlatform.getConfigDirectory].
     */
    @JvmStatic
    fun getConfigDirectory(): Path = FMLPaths.CONFIGDIR.get()
}