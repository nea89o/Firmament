/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.RedirectModifier
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContextBuilder
import com.mojang.brigadier.context.StringRange
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode
import java.util.function.Predicate

class CaseInsensitiveLiteralCommandNode<S>(
    literal: String, command: Command<S>?, requirement: Predicate<S>?,
    redirect: CommandNode<S>?, modifier: RedirectModifier<S>?, forks: Boolean
) : LiteralCommandNode<S>(
    literal.lowercase(), command, requirement, redirect, modifier, forks
) {
    class Builder<S>(literal: String) : LiteralArgumentBuilder<S>(literal) {
        override fun build(): LiteralCommandNode<S> {
            val result = CaseInsensitiveLiteralCommandNode(
                literal,
                command, requirement, redirect, redirectModifier, isFork
            )
            for (argument in arguments) {
                result.addChild(argument)
            }
            return result
        }
    }

    override fun createBuilder(): LiteralArgumentBuilder<S> {
        return Builder<S>(literal).also {
            it.requires(requirement)
            it.forward(redirect, redirectModifier, isFork)
            if (command != null)
                it.executes(command)
        }
    }

    override fun parse(reader: StringReader, contextBuilder: CommandContextBuilder<S>) {
        val start = reader.cursor
        val end = parse0(reader)
        if (end > -1) {
            contextBuilder.withNode(this, StringRange.between(start, end))
            return
        }

        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().createWithContext(reader, literal)
    }

    override fun toString(): String {
        return "<iliteral $literal>"
    }

    private fun parse0(reader: StringReader): Int {
        val start = reader.cursor
        if (reader.canRead(literal.length)) {
            val end = start + literal.length
            if (reader.string.substring(start, end).equals(literal, true)) {
                reader.cursor = end
                if (!reader.canRead() || reader.peek() == ' ') {
                    return end
                } else {
                    reader.cursor = start
                }
            }
        }
        return -1
    }

}
