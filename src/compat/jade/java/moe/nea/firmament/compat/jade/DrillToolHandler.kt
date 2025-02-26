package moe.nea.firmament.compat.jade

import snownee.jade.addon.harvest.ToolHandler
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class DrillToolHandler : ToolHandler {
	override fun test(state: BlockState, world: World, pos: BlockPos): ItemStack {
		TODO("We need to override the existing tool handler tests because they use state.getHardness(world, pos), which doesn't work with Skyblocks NMS fuckery")
	}

	override fun getTools(): List<ItemStack> {
		TODO("Not yet implemented")
	}

	override fun getUid(): Identifier {
		TODO("Not yet implemented")
	}
}
