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

package moe.nea.firmament.commands

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import moe.nea.firmament.util.iterate


typealias DefaultSource = FabricClientCommandSource


inline val <T : CommandContext<*>> T.context get() = this
operator fun <T : Any, C : CommandContext<*>> C.get(arg: TypeSafeArg<T>): T {
    return arg.get(this)
}

fun literal(
    name: String,
    block: LiteralArgumentBuilder<DefaultSource>.() -> Unit
): LiteralArgumentBuilder<DefaultSource> =
    LiteralArgumentBuilder.literal<DefaultSource>(name).also(block)


private fun normalizeGeneric(argument: Type): Class<*> {
    return if (argument is Class<*>) {
        argument
    } else if (argument is TypeVariable<*>) {
        normalizeGeneric(argument.bounds[0])
    } else if (argument is ParameterizedType) {
        normalizeGeneric(argument.rawType)
    } else {
        Any::class.java
    }
}

data class TypeSafeArg<T : Any>(val name: String, val argument: ArgumentType<T>) {
    val argClass by lazy {
        argument.javaClass
            .iterate<Class<in ArgumentType<T>>> {
                it.superclass
            }
            .flatMap {
                it.genericInterfaces.toList()
            }
            .filterIsInstance<ParameterizedType>()
            .find { it.rawType == ArgumentType::class.java }!!
            .let { normalizeGeneric(it.actualTypeArguments[0]) }
    }

    @JvmName("getWithThis")
    fun <S> CommandContext<S>.get(): T =
        get(this)


    fun <S> get(ctx: CommandContext<S>): T {
        return ctx.getArgument(name, argClass) as T
    }
}


fun <T : Any> argument(
    name: String,
    argument: ArgumentType<T>,
    block: RequiredArgumentBuilder<DefaultSource, T>.(TypeSafeArg<T>) -> Unit
): RequiredArgumentBuilder<DefaultSource, T> =
    RequiredArgumentBuilder.argument<DefaultSource, T>(name, argument).also { block(it, TypeSafeArg(name, argument)) }

fun <T : ArgumentBuilder<DefaultSource, T>, AT : Any> T.thenArgument(
    name: String,
    argument: ArgumentType<AT>,
    block: RequiredArgumentBuilder<DefaultSource, AT>.(TypeSafeArg<AT>) -> Unit
): T = then(argument(name, argument, block))

fun <T : RequiredArgumentBuilder<DefaultSource, String>> T.suggestsList(provider: () -> Iterable<String>) {
    suggests(SuggestionProvider<DefaultSource> { context, builder ->
        provider()
            .asSequence()
            .filter { it.startsWith(builder.remaining, ignoreCase = true) }
            .forEach {
                builder.suggest(it)
            }
        builder.buildFuture()
    })
}

fun <T : ArgumentBuilder<DefaultSource, T>> T.thenLiteral(
    name: String,
    block: LiteralArgumentBuilder<DefaultSource>.() -> Unit
): T =
    then(literal(name, block))

fun <T : ArgumentBuilder<DefaultSource, T>> T.then(node: ArgumentBuilder<DefaultSource, *>, block: T.() -> Unit): T =
    then(node).also(block)

fun <T : ArgumentBuilder<DefaultSource, T>> T.thenExecute(block: CommandContext<DefaultSource>.() -> Unit): T =
    executes {
        block(it)
        1
    }


