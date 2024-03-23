/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
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
import moe.nea.firmament.commands.thenLiteral

data class CommandEvent(
    val dispatcher: CommandDispatcher<DefaultSource>,
    val ctx: CommandRegistryAccess,
    val serverCommands: CommandDispatcher<*>?,
) : FirmamentEvent() {
    companion object : FirmamentEventBus<CommandEvent>()

    /**
     * Register subcommands to `/firm`. For new top level commands use [CommandEvent]. Cannot be used to register
     * subcommands to other commands.
     */
    data class SubCommand(
        val builder: CaseInsensitiveLiteralCommandNode.Builder<DefaultSource>,
    ) : FirmamentEvent() {
        companion object : FirmamentEventBus<SubCommand>()

        fun subcommand(name: String, block: CaseInsensitiveLiteralCommandNode.Builder<DefaultSource>.() -> Unit) {
            builder.thenLiteral(name, block)
        }
    }

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
