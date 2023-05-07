package moe.nea.notenoughupdates.util

import io.github.moulberry.repo.data.Coordinate
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos

object MC {
    inline val player get() = MinecraftClient.getInstance().player
    inline val world get() = MinecraftClient.getInstance().world
}

val Coordinate.blockPos: BlockPos
    get() = BlockPos(x, y, z)
