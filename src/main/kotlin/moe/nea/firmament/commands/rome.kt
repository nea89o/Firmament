/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType.string
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.Text
import moe.nea.firmament.features.inventory.storageoverlay.StorageOverlayScreen
import moe.nea.firmament.features.world.FairySouls
import moe.nea.firmament.gui.config.AllConfigsGui
import moe.nea.firmament.gui.profileviewer.ProfileViewer
import moe.nea.firmament.repo.HypixelStaticData
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.ScreenUtil
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.unformattedString


fun firmamentCommand() = literal("firmament") {
    thenLiteral("config") {
        thenExecute {
            AllConfigsGui.showAllGuis()
        }
    }
    thenLiteral("storage") {
        thenExecute {
            ScreenUtil.setScreenLater(StorageOverlayScreen())
            MC.player?.networkHandler?.sendChatCommand("ec")
        }
    }
    thenLiteral("repo") {
        thenLiteral("reload") {
            thenLiteral("fetch") {
                thenExecute {
                    source.sendFeedback(Text.translatable("firmament.repo.reload.network")) // TODO better reporting
                    RepoManager.launchAsyncUpdate()
                }
            }
            thenExecute {
                source.sendFeedback(Text.translatable("firmament.repo.reload.disk"))
                RepoManager.reload()
            }
        }
    }
    thenLiteral("pv") {
        thenExecute {
            ProfileViewer.onCommand(source, MC.player!!.name.unformattedString)
        }
        thenArgument("name", string()) { name ->
            suggestsList {
                MC.world?.players?.filter { it.uuid?.version() == 4 }?.map { it.name.unformattedString } ?: listOf()
            }
            thenExecute {
                ProfileViewer.onCommand(source, get(name))
            }
        }
    }
    thenLiteral("price") {
        thenArgument("item", string()) { item ->
            suggestsList { RepoManager.neuRepo.items.items.keys }
            thenExecute {
                val itemName = SkyblockId(get(item))
                source.sendFeedback(Text.translatable("firmament.price", itemName.neuItem))
                val bazaarData = HypixelStaticData.bazaarData[itemName]
                if (bazaarData != null) {
                    source.sendFeedback(Text.translatable("firmament.price.bazaar"))
                    source.sendFeedback(
                        Text.translatable(
                            "firmament.price.bazaar.productid",
                            bazaarData.productId.bazaarId
                        )
                    )
                    source.sendFeedback(
                        Text.translatable(
                            "firmament.price.bazaar.buy.price",
                            FirmFormatters.toString(bazaarData.quickStatus.buyPrice, 1)
                        )
                    )
                    source.sendFeedback(
                        Text.translatable(
                            "firmament.price.bazaar.buy.order",
                            bazaarData.quickStatus.buyOrders
                        )
                    )
                    source.sendFeedback(
                        Text.translatable(
                            "firmament.price.bazaar.sell.price",
                            FirmFormatters.toString(bazaarData.quickStatus.sellPrice, 1)
                        )
                    )
                    source.sendFeedback(
                        Text.translatable(
                            "firmament.price.bazaar.sell.order",
                            bazaarData.quickStatus.sellOrders
                        )
                    )
                }
                val lowestBin = HypixelStaticData.lowestBin[itemName]
                if (lowestBin != null) {
                    source.sendFeedback(
                        Text.translatable(
                            "firmament.price.lowestbin",
                            FirmFormatters.toString(lowestBin, 1)
                        )
                    )
                }
            }
        }
    }
    thenLiteral("dev") {
        thenLiteral("config") {
            thenExecute {
                FairySouls.TConfig.showConfigEditor()
            }
        }
        thenLiteral("sbdata") {
            thenExecute {
                source.sendFeedback(Text.translatable("firmament.sbinfo.profile", SBData.profileId))
                val locrawInfo = SBData.locraw
                if (locrawInfo == null) {
                    source.sendFeedback(Text.translatable("firmament.sbinfo.nolocraw"))
                } else {
                    source.sendFeedback(Text.translatable("firmament.sbinfo.server", locrawInfo.server))
                    source.sendFeedback(Text.translatable("firmament.sbinfo.gametype", locrawInfo.gametype))
                    source.sendFeedback(Text.translatable("firmament.sbinfo.mode", locrawInfo.mode))
                    source.sendFeedback(Text.translatable("firmament.sbinfo.map", locrawInfo.map))
                }
            }
        }
    }
}


fun registerFirmamentCommand(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
    val firmament = dispatcher.register(firmamentCommand())
    dispatcher.register(literal("firm") {
        redirect(firmament)
    })
}




