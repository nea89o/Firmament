package moe.nea.notenoughupdates.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext
import net.minecraft.client.MinecraftClient

object MinecraftDispatcher : CoroutineDispatcher() {
    @ExperimentalCoroutinesApi
    override fun limitedParallelism(parallelism: Int): CoroutineDispatcher {
        throw UnsupportedOperationException("limitedParallelism is not supported for MinecraftDispatcher")
    }

    override fun isDispatchNeeded(context: CoroutineContext): Boolean =
        !MinecraftClient.getInstance().isOnThread


    override fun dispatch(context: CoroutineContext, block: Runnable) {
        MinecraftClient.getInstance().execute(block)
    }
}
