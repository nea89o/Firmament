package net.examplemod

import dev.architectury.injectables.annotations.ExpectPlatform
import dev.architectury.platform.Platform
import java.nio.file.Path

object ExampleExpectPlatform {
    /**
     * We can use [Platform.getConfigFolder] but this is just an example of [ExpectPlatform].
     *
     *
     * This must be a **public static** method. The platform-implemented solution must be placed under a
     * platform sub-package, with its class suffixed with `Impl`.
     *
     *
     * Example:
     * Expect: net.examplemod.ExampleExpectPlatform#getConfigDirectory()
     * Actual Fabric: net.examplemod.fabric.ExampleExpectPlatformImpl#getConfigDirectory()
     * Actual Forge: net.examplemod.forge.ExampleExpectPlatformImpl#getConfigDirectory()
     *
     *
     * [You should also get the IntelliJ plugin to help with @ExpectPlatform.](https://plugins.jetbrains.com/plugin/16210-architectury)
     */
    @ExpectPlatform
    @JvmStatic
    fun getConfigDirectory(): Path {
        // Just throw an error, the content should get replaced at runtime.
        throw AssertionError()
    }
}
