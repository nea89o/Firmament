
package moe.nea.firmament.events

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

data class AttackBlockEvent(
    val player: PlayerEntity,
    val world: World,
    val hand: Hand,
    val blockPos: BlockPos,
    val direction: Direction
) : FirmamentEvent.Cancellable() {
    companion object : FirmamentEventBus<AttackBlockEvent>()
}
