package moe.nea.firmament.features.items

import io.github.notenoughupdates.moulconfig.ChromaColour
import net.minecraft.world.level.block.Blocks
import net.minecraft.core.Holder
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.level.BlockGetter
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.extraAttributes
import moe.nea.firmament.util.render.RenderInWorldContext
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.skyblock.SkyBlockItems
import moe.nea.firmament.util.tr

object EtherwarpOverlay {
	val identifier: String
		get() = "etherwarp-overlay"

	@Config
	object TConfig : ManagedConfig(identifier, Category.ITEMS) {
		var etherwarpOverlay by toggle("etherwarp-overlay") { false }
		var onlyShowWhileSneaking by toggle("only-show-while-sneaking") { true }
		var cube by toggle("cube") { true }
		val cubeColour by colour("cube-colour") { ChromaColour.fromStaticRGB(172, 0, 255, 60) }
		val failureCubeColour by colour("cube-colour-fail") { ChromaColour.fromStaticRGB(255, 0, 172, 60) }
		val tooCloseCubeColour by colour("cube-colour-tooclose") { ChromaColour.fromStaticRGB(0, 255, 0, 60) }
		val tooFarCubeColour by colour("cube-colour-toofar") { ChromaColour.fromStaticRGB(255, 255, 0, 60) }
		var wireframe by toggle("wireframe") { false }
		var failureText by toggle("failure-text") { false }
	}

	enum class EtherwarpResult(val label: Component?, val color: () -> ChromaColour) {
		SUCCESS(null, TConfig::cubeColour),
		INTERACTION_BLOCKED(
			tr("firmament.etherwarp.fail.tooclosetointeractable", "Too close to interactable"),
			TConfig::tooCloseCubeColour
		),
		TOO_DISTANT(tr("firmament.etherwarp.fail.toofar", "Too far away"), TConfig::tooFarCubeColour),
		OCCUPIED(tr("firmament.etherwarp.fail.occupied", "Occupied"), TConfig::failureCubeColour),
	}

	val interactionBlocked = Checker(
		setOf(
			Blocks.HOPPER,
			Blocks.CHEST,
			Blocks.ENDER_CHEST,
			Blocks.FURNACE,
			Blocks.CRAFTING_TABLE,
			Blocks.CAULDRON,
			Blocks.WATER_CAULDRON,
			Blocks.ENCHANTING_TABLE,
			Blocks.DISPENSER,
			Blocks.DROPPER,
			Blocks.BREWING_STAND,
			Blocks.TRAPPED_CHEST,
			Blocks.LEVER,
		),
		setOf(
			BlockTags.DOORS,
			BlockTags.TRAPDOORS,
			BlockTags.ANVIL,
			BlockTags.FENCE_GATES,
		)
	)

	data class Checker<T>(
        val direct: Set<T>,
        val byTag: Set<TagKey<T>>,
	) {
		fun matches(entry: Holder<T>): Boolean {
			return entry.value() in direct || checkTags(entry, byTag)
		}
	}

	val etherwarpHallpasses = Checker(
		setOf(
			Blocks.CREEPER_HEAD,
			Blocks.CREEPER_WALL_HEAD,
			Blocks.DRAGON_HEAD,
			Blocks.DRAGON_WALL_HEAD,
			Blocks.SKELETON_SKULL,
			Blocks.SKELETON_WALL_SKULL,
			Blocks.WITHER_SKELETON_SKULL,
			Blocks.WITHER_SKELETON_WALL_SKULL,
			Blocks.PIGLIN_HEAD,
			Blocks.PIGLIN_WALL_HEAD,
			Blocks.ZOMBIE_HEAD,
			Blocks.ZOMBIE_WALL_HEAD,
			Blocks.PLAYER_HEAD,
			Blocks.PLAYER_WALL_HEAD,
			Blocks.REPEATER,
			Blocks.COMPARATOR,
			Blocks.BIG_DRIPLEAF_STEM,
			Blocks.MOSS_CARPET,
			Blocks.PALE_MOSS_CARPET,
			Blocks.COCOA,
			Blocks.LADDER,
			Blocks.SEA_PICKLE,
		),
		setOf(
			BlockTags.FLOWER_POTS,
			BlockTags.WOOL_CARPETS,
		),
	)
	val etherwarpConsidersFat = Checker(
		setOf(), setOf(
			// Wall signs have a hitbox
			BlockTags.ALL_SIGNS, BlockTags.ALL_HANGING_SIGNS,
			BlockTags.BANNERS,
		)
	)


	fun <T> checkTags(holder: Holder<out T>, set: Set<TagKey<out T>>) =
		holder.tags()
			.anyMatch(set::contains)


	fun isEtherwarpTransparent(world: BlockGetter, blockPos: BlockPos): Boolean {
		val blockState = world.getBlockState(blockPos)
		val block = blockState.block
		if (etherwarpConsidersFat.matches(blockState.blockHolder))
			return false
		if (block.defaultBlockState().getCollisionShape(world, blockPos).isEmpty)
			return true
		if (etherwarpHallpasses.matches(blockState.blockHolder))
			return true
		return false
	}

	sealed interface EtherwarpBlockHit {
		data class BlockHit(val blockPos: BlockPos, val accuratePos: Vec3?) : EtherwarpBlockHit
		data object Miss : EtherwarpBlockHit
	}

	fun raycastWithEtherwarpTransparency(world: BlockGetter, start: Vec3, end: Vec3): EtherwarpBlockHit {
		return BlockGetter.traverseBlocks<EtherwarpBlockHit, Unit>(
			start, end, Unit,
			{ _, blockPos ->
				if (isEtherwarpTransparent(world, blockPos)) {
					return@traverseBlocks null
				}
//				val defaultedState = world.getBlockState(blockPos).block.defaultState
//				val hitShape = defaultedState.getCollisionShape(
//					world,
//					blockPos,
//					ShapeContext.absent()
//				)
//				if (world.raycastBlock(start, end, blockPos, hitShape, defaultedState) == null) {
//					return@raycast null
//				}
				val partialResult = world.clipWithInteractionOverride(start, end, blockPos, Shapes.block(), world.getBlockState(blockPos).block.defaultBlockState())
				return@traverseBlocks EtherwarpBlockHit.BlockHit(blockPos, partialResult?.location)
			},
			{ EtherwarpBlockHit.Miss })
	}

	enum class EtherwarpItemKind {
		MERGED,
		RAW
	}

	@Subscribe
	fun renderEtherwarpOverlay(event: WorldRenderLastEvent) {
		if (!TConfig.etherwarpOverlay) return
		val player = MC.player ?: return
		if (TConfig.onlyShowWhileSneaking && !player.isShiftKeyDown) return
		val world = player.level
		val heldItem = MC.stackInHand
		val etherwarpTyp = run {
			if (heldItem.extraAttributes.contains("ethermerge"))
				EtherwarpItemKind.MERGED
			else if (heldItem.skyBlockId == SkyBlockItems.ETHERWARP_CONDUIT)
				EtherwarpItemKind.RAW
			else
				return
		}
		val playerEyeHeight = // Sneaking: 1.27 (1.21) 1.54 (1.8.9) / Upright: 1.62 (1.8.9,1.21)
			if (player.isShiftKeyDown || etherwarpTyp == EtherwarpItemKind.MERGED)
				(if (SBData.skyblockLocation?.isModernServer ?: false) 1.27 else 1.54)
			else 1.62
		val playerEyePos = player.position.add(0.0, playerEyeHeight, 0.0)
		val start = playerEyePos
		val end = player.getViewVector(0F).scale(160.0).add(playerEyePos)
		val hitResult = raycastWithEtherwarpTransparency(
			world,
			start,
			end,
		)
		if (hitResult !is EtherwarpBlockHit.BlockHit) return
		val blockPos = hitResult.blockPos
		val success = run {
			if (!isEtherwarpTransparent(world, blockPos.above()))
				EtherwarpResult.OCCUPIED
			else if (!isEtherwarpTransparent(world, blockPos.above(2)))
				EtherwarpResult.OCCUPIED
			else if (playerEyePos.distanceToSqr(hitResult.accuratePos ?: blockPos.center) > 61 * 61)
				EtherwarpResult.TOO_DISTANT
			else if ((MC.instance.hitResult as? BlockHitResult)
					?.takeIf { it.type == HitResult.Type.BLOCK }
					?.let { interactionBlocked.matches(world.getBlockState(it.blockPos).blockHolder) }
					?: false
			)
				EtherwarpResult.INTERACTION_BLOCKED
			else
				EtherwarpResult.SUCCESS
		}
		RenderInWorldContext.renderInWorld(event) {
			if (TConfig.cube)
				block(
					blockPos,
					success.color().getEffectiveColourRGB()
				)
			if (TConfig.wireframe) wireframeCube(blockPos, 10f)
			if (TConfig.failureText && success.label != null) {
				withFacingThePlayer(blockPos.center) {
					text(success.label)
				}
			}
		}
	}
}
