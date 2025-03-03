package moe.nea.firmament.compat.jade

import com.google.common.collect.Lists
import snownee.jade.addon.harvest.SimpleToolHandler
import snownee.jade.addon.harvest.ToolHandler
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import moe.nea.firmament.util.SBData

class DrillToolHandler(
	private val uid: Identifier,
	private val tools: MutableList<ItemStack>
) : ToolHandler {
	override fun test(state: BlockState, world: World, pos: BlockPos): ItemStack {
		if (isOnMiningIsland()) {
		}

		// TODO: figure out how this work
		return SimpleToolHandler.create(uid, tools.map {
			return@map it.item
		}).test(state, world, pos)
	}

	override fun getTools(): List<ItemStack> {
		return this.tools
	}

	override fun getUid(): Identifier {
		return this.uid
	}
}
