package moe.nea.firmament.events

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.world.World

data class UseItemEvent(val playerEntity: PlayerEntity, val world: World, val hand: Hand) : FirmamentEvent.Cancellable() {
	companion object : FirmamentEventBus<UseItemEvent>()
	val item: ItemStack = playerEntity.getStackInHand(hand)
}
