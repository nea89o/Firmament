package moe.nea.firmament.compat.jade

import com.google.common.collect.Lists
import snownee.jade.addon.harvest.ToolHandler
import net.minecraft.block.BlockState
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class SimpleToolHandler(
	private val uid: Identifier,
	private val tools: MutableList<ItemStack>
) : ToolHandler {
	constructor(uid: Identifier, tools: MutableList<Item>) : this(uid, Lists.transform(tools, Item::getDefaultStack))

	override fun test(state: BlockState, world: World, pos: BlockPos): ItemStack {
		TODO("We need to override the existing tool handler tests because they use state.getHardness(world, pos) instead of using Breaking Power")
	}

	override fun getTools(): List<ItemStack> {
		return this.tools
	}

	override fun getUid(): Identifier {
		return this.uid
	}
}
