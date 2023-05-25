/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package moe.nea.firmament.rei

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
        val focusedSlot = screen.focusedSlot_NEU ?: return CompoundEventResult.pass()
        val item = focusedSlot.stack ?: return CompoundEventResult.pass()
        val skyblockId = item.skyBlockId ?: return CompoundEventResult.pass()
        return CompoundEventResult.interruptTrue(SBItemEntryDefinition.getEntry(skyblockId))
    }

    override fun getPriority(): Double = 1_000_000.0
}
