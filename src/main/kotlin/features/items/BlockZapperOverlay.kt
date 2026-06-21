package moe.nea.firmament.features.items

import io.github.notenoughupdates.moulconfig.ChromaColour
import java.util.LinkedList
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.core.BlockPos
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.WorldKeyboardEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.mc.accessor
import moe.nea.firmament.util.render.RenderInWorldContext
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.skyblock.SkyBlockItems

object BlockZapperOverlay {
	val identifier: String
		get() = "block-zapper-overlay"

	@Config
	object TConfig : ManagedConfig(identifier, Category.ITEMS) {
		var blockZapperOverlay by toggle("block-zapper-overlay") { false }
		val color by colour("color") { ChromaColour.fromStaticRGB(160, 0, 0, 60) }
		var undoKey by keyBindingWithDefaultUnbound("undo-key")
	}

	val bannedZapper: List<Block> = listOf<Block>(
		Blocks.WHEAT,
		Blocks.CARROTS,
		Blocks.POTATOES,
		Blocks.PUMPKIN,
		Blocks.PUMPKIN_STEM,
		Blocks.MELON,
		Blocks.MELON_STEM,
		Blocks.CACTUS,
		Blocks.SUGAR_CANE,
		Blocks.NETHER_WART,
		Blocks.TALL_GRASS,
		Blocks.SUNFLOWER,
		Blocks.FARMLAND,
		Blocks.BREWING_STAND,
		Blocks.SNOW,
		Blocks.RED_MUSHROOM,
		Blocks.BROWN_MUSHROOM,
	)

	private val zapperOffsets: List<BlockPos> = listOf(
		BlockPos(0, 0, -1),
		BlockPos(0, 0, 1),
		BlockPos(-1, 0, 0),
		BlockPos(1, 0, 0),
		BlockPos(0, 1, 0),
		BlockPos(0, -1, 0)
	)

	// Skidded from NEU
	// Credit: https://github.com/NotEnoughUpdates/NotEnoughUpdates/blob/9b1fcfebc646e9fb69f99006327faa3e734e5f51/src/main/java/io/github/moulberry/notenoughupdates/miscfeatures/CustomItemEffects.java#L1281-L1355 (Modified)
	@Subscribe
	fun renderBlockZapperOverlay(event: WorldRenderLastEvent) {
		if (!TConfig.blockZapperOverlay) return
		val player = MC.player ?: return
		val world = player.level ?: return
		val heldItem = MC.stackInHand
		if (heldItem.accessor().skyBlockId != SkyBlockItems.BLOCK_ZAPPER) return
		val hitResult = MC.instance.hitResult ?: return

		val zapperBlocks: HashSet<BlockPos> = HashSet()
		val returnablePositions = LinkedList<BlockPos>()

		if (hitResult is BlockHitResult && hitResult.type == HitResult.Type.BLOCK) {
			var pos: BlockPos = hitResult.blockPos
			val firstBlockState: BlockState = world.getBlockState(pos)
			val block = firstBlockState.block

			val initialAboveBlock = world.getBlockState(pos.above()).block
			if (!bannedZapper.contains(initialAboveBlock) && !bannedZapper.contains(block)) {
				var i = 0
				while (i < 164) {
					zapperBlocks.add(pos)
					returnablePositions.remove(pos)

					val availableNeighbors: MutableList<BlockPos> = ArrayList()

					for (offset in zapperOffsets) {
						val newPos = pos.offset(offset)

						if (zapperBlocks.contains(newPos)) continue

						val state: BlockState? = world.getBlockState(newPos)
						if (state != null && state.block === block) {
							val above = newPos.above()
							val aboveBlock = world.getBlockState(above).block
							if (!bannedZapper.contains(aboveBlock)) {
								availableNeighbors.add(newPos)
							}
						}
					}

					if (availableNeighbors.size >= 2) {
						returnablePositions.add(pos)
						pos = availableNeighbors[0]
					} else if (availableNeighbors.size == 1) {
						pos = availableNeighbors[0]
					} else if (returnablePositions.isEmpty()) {
						break
					} else {
						i--
						pos = returnablePositions.last()
					}

					i++
				}
			}

			RenderInWorldContext.renderInWorld(event) {
				if (MC.player?.isShiftKeyDown ?: false) {
					zapperBlocks.forEach {
						block(it, TConfig.color.getEffectiveColourRGB())
					}
				} else {
					sharedVoxelSurface(zapperBlocks, TConfig.color.getEffectiveColourRGB())
				}
			}
		}
	}

	@Subscribe
	fun onWorldKeyboard(it: WorldKeyboardEvent) {
		if (!TConfig.undoKey.isBound) return
		if (!it.matches(TConfig.undoKey)) return
		if (MC.stackInHand.accessor().skyBlockId != SkyBlockItems.BLOCK_ZAPPER) return
		MC.sendCommand("undozap")
	}
}
