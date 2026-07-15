package moe.nea.firmament.features.texturepack

import com.mojang.brigadier.arguments.IntegerArgumentType
import net.minecraft.world.level.block.Block
import net.minecraft.commands.arguments.blocks.BlockStateArgument
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.get
import moe.nea.firmament.commands.thenArgument
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.commands.thenLiteral
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.features.debug.DeveloperFeatures
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.render.RenderInWorldContext
import moe.nea.firmament.util.tr

object CustomBlockTexturesDebugger {
	var debugMode: DebugMode = DebugMode.Never
	var range = 30


	@Subscribe
	fun onRender(event: WorldRenderLastEvent) {
		if (debugMode == DebugMode.Never) return
		val replacements = CustomBlockTextures.currentIslandReplacements ?: return
		RenderInWorldContext.renderInWorld(event) {
			for ((block, repl) in replacements.lookup) {
				if (!debugMode.shouldHighlight(block)) continue
				for (i in repl) {
					if (i.roughCheck != null)
						tryRenderBox(i.roughCheck!!.toBox(), 0x50FF8050.toInt())
					i.checks?.forEach { area ->
						tryRenderBox(area.toBox(), 0x5050FF50.toInt())
					}
				}
			}
		}
	}

	fun RenderInWorldContext.tryRenderBox(box: AABB, colour: Int) {
		val player = MC.player?.position() ?: Vec3.ZERO
		if (box.center.distanceTo(player) < range + maxOf(
				box.zsize, box.xsize, box.ysize
			) / 2 && !box.contains(player)
		) {
			box(box, colour)
		}
	}


	@Subscribe
	fun onCommand(event: CommandEvent.SubCommand) {
		event.subcommand(DeveloperFeatures.DEVELOPER_SUBCOMMAND) {
			thenLiteral("debugcbt") {
				thenLiteral("range") {
					thenArgument("range", IntegerArgumentType.integer(0)) { rangeArg ->
						thenExecute {
							this@CustomBlockTexturesDebugger.range = get(rangeArg)
							MC.sendChat(
								tr(
									"firmament.debugcbt.always",
									"Only render areas within ${this@CustomBlockTexturesDebugger.range} blocks"
								)
							)
						}
					}
				}
				thenLiteral("all") {
					thenExecute {
						debugMode = DebugMode.Always
						MC.sendChat(
							tr(
								"firmament.debugcbt.always",
								"Showing debug outlines for all custom block textures"
							)
						)
					}
				}
				thenArgument("block", BlockStateArgument.block(event.commandRegistryAccess)) { block ->
					thenExecute {
						val block = get(block).state.block
						debugMode = DebugMode.ForBlock(block)
						MC.sendChat(
							tr(
								"firmament.debugcbt.block",
								"Showing debug outlines for all custom ${block.name} textures"
							)
						)
					}
				}
				thenLiteral("never") {
					thenExecute {
						debugMode = DebugMode.Never
						MC.sendChat(
							tr(
								"firmament.debugcbt.disabled",
								"Disabled debug outlines for custom block textures"
							)
						)
					}
				}
			}
		}
	}

	sealed interface DebugMode {
		fun shouldHighlight(block: Block): Boolean

		data object Never : DebugMode {
			override fun shouldHighlight(block: Block): Boolean {
				return false
			}
		}

		data class ForBlock(val block: Block) : DebugMode {
			override fun shouldHighlight(block: Block): Boolean {
				return block == this.block
			}
		}

		data object Always : DebugMode {
			override fun shouldHighlight(block: Block): Boolean {
				return true
			}
		}
	}
}
