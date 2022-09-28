package moe.nea.notenoughupdates.util

import net.minecraft.client.MinecraftClient

object MC {
    inline val player get() = MinecraftClient.getInstance().player
}
