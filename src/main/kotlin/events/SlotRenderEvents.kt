

package moe.nea.firmament.events

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.texture.Sprite
import net.minecraft.screen.slot.Slot
import net.minecraft.util.Identifier
import moe.nea.firmament.util.MC

interface SlotRenderEvents {
    val context: DrawContext
    val slot: Slot
    val mouseX: Int
    val mouseY: Int
    val delta: Float

	fun highlight(sprite: Sprite) {
		context.drawSprite(
			slot.x, slot.y, 0, 16, 16,
			sprite
		)
	}

    data class Before(
        override val context: DrawContext, override val slot: Slot,
        override val mouseX: Int,
        override val mouseY: Int,
        override val delta: Float
    ) : FirmamentEvent(),
        SlotRenderEvents {
        companion object : FirmamentEventBus<Before>()
    }

    data class After(
        override val context: DrawContext, override val slot: Slot,
        override val mouseX: Int,
        override val mouseY: Int,
        override val delta: Float
    ) : FirmamentEvent(),
        SlotRenderEvents {
        companion object : FirmamentEventBus<After>()
    }
}
