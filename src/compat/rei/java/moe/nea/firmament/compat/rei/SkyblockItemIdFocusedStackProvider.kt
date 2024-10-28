

package moe.nea.firmament.compat.rei

import dev.architectury.event.CompoundEventResult
import me.shedaniel.math.Point
import me.shedaniel.rei.api.client.registry.screen.FocusedStackProvider
import me.shedaniel.rei.api.common.entry.EntryStack
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen
import moe.nea.firmament.util.skyBlockId

object SkyblockItemIdFocusedStackProvider : FocusedStackProvider {
    override fun provide(screen: Screen?, mouse: Point?): CompoundEventResult<EntryStack<*>> {
        if (screen !is HandledScreen<*>) return CompoundEventResult.pass()
        screen as AccessorHandledScreen
        val focusedSlot = screen.focusedSlot_Firmament ?: return CompoundEventResult.pass()
        val item = focusedSlot.stack ?: return CompoundEventResult.pass()
        val skyblockId = item.skyBlockId ?: return CompoundEventResult.pass()
        return CompoundEventResult.interruptTrue(SBItemEntryDefinition.getEntry(skyblockId))
    }

    override fun getPriority(): Double = 1_000_000.0
}
