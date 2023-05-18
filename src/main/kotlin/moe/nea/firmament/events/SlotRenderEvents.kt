package moe.nea.firmament.events

import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.slot.Slot

interface SlotRenderEvents {
    val matrices: MatrixStack
    val slot: Slot
    val mouseX: Int
    val mouseY: Int
    val delta: Float

    data class Before(
        override val matrices: MatrixStack, override val slot: Slot,
        override val mouseX: Int,
        override val mouseY: Int,
        override val delta: Float
    ) : FirmamentEvent(),
        SlotRenderEvents {
        companion object : FirmamentEventBus<Before>()
    }

    data class After(
        override val matrices: MatrixStack, override val slot: Slot,
        override val mouseX: Int,
        override val mouseY: Int,
        override val delta: Float
    ) : FirmamentEvent(),
        SlotRenderEvents {
        companion object : FirmamentEventBus<After>()
    }
}
