package moe.nea.firmament.events

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.state.level.CameraRenderState

/**
 * This event is called after all world rendering is done, but before any GUI rendering (including hand) has been done.
 */
data class WorldRenderLastEvent(
	val matrices: PoseStack,
	val camera: CameraRenderState
) : FirmamentEvent() {
	companion object : FirmamentEventBus<WorldRenderLastEvent>()
}
