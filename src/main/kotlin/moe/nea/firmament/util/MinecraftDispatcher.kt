

package moe.nea.firmament.util

import kotlinx.coroutines.asCoroutineDispatcher
import net.minecraft.client.MinecraftClient

val MinecraftDispatcher by lazy { MinecraftClient.getInstance().asCoroutineDispatcher() }
