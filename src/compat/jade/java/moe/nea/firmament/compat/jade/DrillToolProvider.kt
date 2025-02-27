package moe.nea.firmament.compat.jade

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.collect.ImmutableList
import com.google.common.collect.Maps
import java.util.concurrent.TimeUnit
import snownee.jade.addon.harvest.ToolHandler
import snownee.jade.api.BlockAccessor
import snownee.jade.api.IBlockComponentProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.SynchronousResourceReloader
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.Vec2f


class DrillToolProvider : IBlockComponentProvider, SynchronousResourceReloader {
	val resultCache: Cache<BlockState, ImmutableList<ItemStack>> = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build()
	val toolHandlers: MutableMap<Identifier, ToolHandler> = Maps.newLinkedHashMap()
	private val shearableBlocks: MutableList<Block> = mutableListOf()
	private val checkIcon: Text = Text.literal("✔")
	private val xIcon: Text = Text.literal("✕")
	private val itemSize = Vec2f(10f, 0f)

	@Synchronized
	fun registerHandler(handler: ToolHandler) {
		toolHandlers.put(handler.uid, handler)
	}

	override fun appendTooltip(tooltip: ITooltip, accessor: BlockAccessor, config: IPluginConfig) {
		TODO("Not yet implemented")
	}

	override fun getUid(): Identifier {
		TODO("Not yet implemented")
	}

	override fun reload(manager: ResourceManager) {
		TODO("Not yet implemented")
	}
}
