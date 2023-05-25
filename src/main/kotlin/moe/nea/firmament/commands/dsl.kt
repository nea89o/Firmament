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
import java.lang.reflect.ParameterizedType
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

data class TypeSafeArg<T : Any>(val name: String, val argument: ArgumentType<T>) {
    val argClass by lazy {
        argument.javaClass
            .iterate<Class<in ArgumentType<T>>> {
                it.superclass
            }
            .map {
                it.genericSuperclass
            }
            .filterIsInstance<ParameterizedType>()
            .find { it.rawType == ArgumentType::class.java }!!
            .let { it.actualTypeArguments[0] as Class<*> }
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


