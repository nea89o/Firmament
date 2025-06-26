package moe.nea.firmament.features.items

import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import net.minecraft.util.hit.BlockHitResult
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.util.extraAttributes
import moe.nea.firmament.util.render.RenderInWorldContext
import moe.nea.firmament.util.skyBlockId

object EtherwarpOverlay : FirmamentFeature {
	override val identifier: String
		get() = "etherwarp-overlay"

	object TConfig : ManagedConfig(identifier, Category.ITEMS) {
		var etherwarpOverlay by toggle("etherwarp-overlay") { false }
		var cube by toggle("cube") { true }
		var wireframe by toggle("wireframe") { false }
	}

	override val config: ManagedConfig
		get() = TConfig


	@Subscribe
	fun renderEtherwarpOverlay(event: WorldRenderLastEvent) {
		val player =
			MC.player?.takeIf { MC.world != null && MC.camera != null && TConfig.etherwarpOverlay && it.isSneaking }
				?: return
		player.mainHandStack.skyBlockId?.takeIf { it.neuItem == "ASPECT_OF_THE_VOID" || it.neuItem == "ASPECT_OF_THE_END" }
			?: return
		if (player.mainHandStack.extraAttributes.get("ethermerge") == null) return
		val camera = MC.camera ?: return
		val world = MC.world ?: return

		val hitResult = camera.raycast(61.0, 0.0f, false)
		if (hitResult is BlockHitResult) {
			val blockPos = hitResult.blockPos
			if (camera.squaredDistanceTo(blockPos.toCenterPos()) <= 61.0 * 61.0 && world.getBlockState(blockPos.up()).isAir && world.getBlockState(
					blockPos.up(2)
				).isAir
			) {
				RenderInWorldContext.renderInWorld(event) {
					if (TConfig.cube) block(blockPos, 0xFFFFFF00.toInt())
					if (TConfig.wireframe) wireframeCube(blockPos, 10f)
				}
			}

		}
	}
}
