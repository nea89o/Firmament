package moe.nea.firmament.events

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import net.minecraft.resource.ReloadableResourceManagerImpl
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloader
import net.minecraft.util.profiler.Profiler

data class FinalizeResourceManagerEvent(
    val resourceManager: ReloadableResourceManagerImpl,
) : FirmamentEvent() {
    companion object : FirmamentEventBus<FinalizeResourceManagerEvent>()

    inline fun registerOnApply(name: String, crossinline function: () -> Unit) {
        resourceManager.registerReloader(object : ResourceReloader {
            override fun reload(
                synchronizer: ResourceReloader.Synchronizer,
                manager: ResourceManager?,
                prepareProfiler: Profiler?,
                applyProfiler: Profiler?,
                prepareExecutor: Executor?,
                applyExecutor: Executor
            ): CompletableFuture<Void> {
                return CompletableFuture.completedFuture(Unit)
                    .thenCompose(synchronizer::whenPrepared)
                    .thenAcceptAsync({ function() }, applyExecutor)
            }

            override fun getName(): String {
                return name
            }
        })
    }
}
