

package moe.nea.firmament.events

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.texture.Sprite
import net.minecraft.screen.slot.Slot
import net.minecraft.util.Identifier
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.render.drawGuiTexture

interface SlotRenderEvents {
    val context: DrawContext
    val slot: Slot

	fun highlight(sprite: Identifier) {
		context.drawGuiTexture(
			slot.x, slot.y, 0, 16, 16,
			sprite
		)
	}

    data class Before(
        override val context: DrawContext, override val slot: Slot,
    ) : FirmamentEvent(),
        SlotRenderEvents {
        companion object : FirmamentEventBus<Before>()
    }

    data class After(
        override val context: DrawContext, override val slot: Slot,
    ) : FirmamentEvent(),
        SlotRenderEvents {
        companion object : FirmamentEventBus<After>()
    }
}
