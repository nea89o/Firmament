/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.commands

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import kotlinx.coroutines.launch
import moe.nea.firmament.Firmament
import moe.nea.firmament.util.MinecraftDispatcher
import moe.nea.firmament.util.iterate
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable


typealias DefaultSource = FabricClientCommandSource


inline val <T : CommandContext<*>> T.context get() = this
operator fun <T : Any, C : CommandContext<*>> C.get(arg: TypeSafeArg<T>): T {
    return arg.get(this)
}

fun literal(
    name: String,
    block: CaseInsensitiveLiteralCommandNode.Builder<DefaultSource>.() -> Unit
): CaseInsensitiveLiteralCommandNode.Builder<DefaultSource> =
    CaseInsensitiveLiteralCommandNode.Builder<DefaultSource>(name).also(block)


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
        try {
            return ctx.getArgument(name, argClass) as T
        } catch (e: Exception) {
            if (ctx.child != null) {
                return get(ctx.child)
            }
            throw e
        }
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

fun <T : RequiredArgumentBuilder<DefaultSource, String>> T.suggestsList(provider: CommandContext<DefaultSource>.() -> Iterable<String>) {
    suggests(SuggestionProvider<DefaultSource> { context, builder ->
        provider(context)
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
    block: CaseInsensitiveLiteralCommandNode.Builder<DefaultSource>.() -> Unit
): T =
    then(literal(name, block))

fun <T : ArgumentBuilder<DefaultSource, T>> T.then(node: ArgumentBuilder<DefaultSource, *>, block: T.() -> Unit): T =
    then(node).also(block)

fun <T : ArgumentBuilder<DefaultSource, T>> T.thenExecute(block: suspend CommandContext<DefaultSource>.() -> Unit): T =
    executes {
        Firmament.coroutineScope.launch(MinecraftDispatcher) {
            block(it)
        }
        1
    }


