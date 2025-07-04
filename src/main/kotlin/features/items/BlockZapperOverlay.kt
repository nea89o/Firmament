package moe.nea.firmament.features.items

import java.util.LinkedList
import me.shedaniel.math.Color
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ClientStartedEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.render.RenderInWorldContext
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.skyblock.SkyBlockItems

object BlockZapperOverlay : FirmamentFeature {
	override val identifier: String
		get() = "block-zapper-overlay"

	object TConfig : ManagedConfig(identifier, Category.ITEMS) {
		var blockZapperOverlay by toggle("block-zapper-overlay") { false }
	}

	@Subscribe
	fun onInit(event: ClientStartedEvent) {
	}

	override val config: ManagedConfig
		get() = TConfig

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
		val world = player.world ?: return
		val heldItem = MC.stackInHand
		if (heldItem.skyBlockId != SkyBlockItems.BLOCK_ZAPPER) return
		val hitResult = MC.instance.crosshairTarget ?: return

		val zapperBlocks: HashSet<BlockPos> = HashSet()
		val returnablePositions = LinkedList<BlockPos>()

		if (hitResult is BlockHitResult && hitResult.type == HitResult.Type.BLOCK) {
			var pos: BlockPos = hitResult.blockPos
			val firstBlockState: BlockState = world.getBlockState(pos)
			val block = firstBlockState.block

			val initialAboveBlock = world.getBlockState(pos.up()).block
			if (!bannedZapper.contains(initialAboveBlock) && !bannedZapper.contains(block)) {
				var i = 0
				while (i < 164) {
					zapperBlocks.add(pos)
					returnablePositions.remove(pos)

					val availableNeighbors: MutableList<BlockPos> = ArrayList()

					for (offset in zapperOffsets) {
						val newPos = pos.add(offset)

						if (zapperBlocks.contains(newPos)) continue

						val state: BlockState? = world.getBlockState(newPos)
						if (state != null && state.block === block) {
							val above = newPos.up()
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
				if (MC.player?.isSneaking ?: false) {
					zapperBlocks.forEach {
						block(it, Color.ofRGBA(255, 0, 0, 60).color)
					}
				} else {
					sharedVoxelSurface(zapperBlocks, Color.ofRGBA(255, 0, 0, 60).color)
				}
			}
		}
	}
}
