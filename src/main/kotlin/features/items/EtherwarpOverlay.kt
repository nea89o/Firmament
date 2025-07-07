package moe.nea.firmament.features.items

import io.github.notenoughupdates.moulconfig.ChromaColour
import me.shedaniel.math.Color
import net.minecraft.util.hit.BlockHitResult
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.extraAttributes
import moe.nea.firmament.util.render.RenderInWorldContext
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.skyblock.SkyBlockItems

object EtherwarpOverlay : FirmamentFeature {
	override val identifier: String
		get() = "etherwarp-overlay"

	object TConfig : ManagedConfig(identifier, Category.ITEMS) {
		var etherwarpOverlay by toggle("etherwarp-overlay") { false }
		var onlyShowWhileSneaking by toggle("only-show-while-sneaking") { true }
		var cube by toggle("cube") { true }
		val cubeColour by colour("cube-colour") { ChromaColour.fromStaticRGB(172, 0, 255, 60) }
		var wireframe by toggle("wireframe") { false }
	}

	override val config: ManagedConfig
		get() = TConfig


	@Subscribe
	fun renderEtherwarpOverlay(event: WorldRenderLastEvent) {
		if (!TConfig.etherwarpOverlay) return
		val player = MC.player ?: return
		if (TConfig.onlyShowWhileSneaking && !player.isSneaking) return
		val world = player.world
		val camera = MC.camera ?: return
		val heldItem = MC.stackInHand
		if (heldItem.skyBlockId !in listOf(SkyBlockItems.ASPECT_OF_THE_VOID, SkyBlockItems.ASPECT_OF_THE_END)) return
		if (!heldItem.extraAttributes.contains("ethermerge")) return

		val hitResult = camera.raycast(61.0, 0.0f, false)
		if (hitResult !is BlockHitResult) return
		val blockPos = hitResult.blockPos
		if (camera.squaredDistanceTo(blockPos.toCenterPos()) > 61 * 61) return
		if (!world.getBlockState(blockPos.up()).isAir) return
		if (!world.getBlockState(blockPos.up(2)).isAir) return
		RenderInWorldContext.renderInWorld(event) {
			if (TConfig.cube) block(blockPos, TConfig.cubeColour.getEffectiveColourRGB())
			if (TConfig.wireframe) wireframeCube(blockPos, 10f)
		}
	}
}
