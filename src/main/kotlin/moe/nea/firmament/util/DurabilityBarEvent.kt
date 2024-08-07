
package moe.nea.firmament.util

import me.shedaniel.math.Color
import net.minecraft.item.ItemStack
import moe.nea.firmament.events.FirmamentEvent
import moe.nea.firmament.events.FirmamentEventBus

data class DurabilityBarEvent(
    val item: ItemStack,
) : FirmamentEvent() {
    data class DurabilityBar(
        val color: Color,
        val percentage: Float,
    )

    var barOverride: DurabilityBar? = null

    companion object : FirmamentEventBus<DurabilityBarEvent>()
}
