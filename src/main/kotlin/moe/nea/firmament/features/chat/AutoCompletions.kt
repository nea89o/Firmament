/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.chat

import com.mojang.brigadier.arguments.StringArgumentType.string
import moe.nea.firmament.commands.get
import moe.nea.firmament.commands.suggestsList
import moe.nea.firmament.commands.thenArgument
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.MaskCommands
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.MC

object AutoCompletions : FirmamentFeature {

    object TConfig : ManagedConfig(identifier) {
        val provideWarpTabCompletion by toggle("warp-complete") { true }
        val replaceWarpIsByWarpIsland by toggle("warp-is") { true }
    }

    override val config: ManagedConfig?
        get() = TConfig
    override val identifier: String
        get() = "auto-completions"

    override fun onLoad() {
        MaskCommands.subscribe {
            if (TConfig.provideWarpTabCompletion) {
                it.mask("warp")
            }
        }
        CommandEvent.subscribe {
            if (TConfig.provideWarpTabCompletion) {
                it.deleteCommand("warp")
                it.register("warp") {
                    thenArgument("to", string()) { toArg ->
                        suggestsList {
                            RepoManager.neuRepo.constants?.islands?.warps?.flatMap { listOf(it.warp) + it.aliases } ?: listOf()
                        }
                        thenExecute {
                            val warpName = get(toArg)
                            if (warpName == "is" && TConfig.replaceWarpIsByWarpIsland) {
                                MC.sendServerCommand("warp island")
                            } else {
                                MC.sendServerCommand("warp ${warpName}")
                            }
                        }
                    }
                }
            }
        }
    }
}
