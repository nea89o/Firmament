package moe.nea.firmament.util

import io.github.moulberry.repo.data.Coordinate
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.util.math.BlockPos
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen

object MC {
    inline val soundManager get() = MinecraftClient.getInstance().soundManager
    inline val player get() = MinecraftClient.getInstance().player
    inline val world get() = MinecraftClient.getInstance().world
    inline val screen get() = MinecraftClient.getInstance().currentScreen
    inline val handledScreen: HandledScreen<*>? get() = MinecraftClient.getInstance().currentScreen as? HandledScreen<*>
}

val Coordinate.blockPos: BlockPos
    get() = BlockPos(x, y, z)
