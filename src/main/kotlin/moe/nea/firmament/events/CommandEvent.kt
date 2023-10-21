/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.LiteralCommandNode
import net.minecraft.command.CommandRegistryAccess
import moe.nea.firmament.commands.CaseInsensitiveLiteralCommandNode
import moe.nea.firmament.commands.DefaultSource
import moe.nea.firmament.commands.literal

data class CommandEvent(
    val dispatcher: CommandDispatcher<DefaultSource>,
    val ctx: CommandRegistryAccess,
    val serverCommands: CommandDispatcher<*>?,
) : FirmamentEvent() {
    companion object : FirmamentEventBus<CommandEvent>()

    fun deleteCommand(name: String) {
        dispatcher.root.children.removeIf { it.name.equals(name, ignoreCase = false) }
        serverCommands?.root?.children?.removeIf { it.name.equals(name, ignoreCase = false) }
    }

    fun register(
        name: String,
        block: CaseInsensitiveLiteralCommandNode.Builder<DefaultSource>.() -> Unit
    ): LiteralCommandNode<DefaultSource> {
        return dispatcher.register(literal(name, block))
    }
}
