/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType.string
import io.ktor.client.statement.*
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.Text
import moe.nea.firmament.apis.UrsaManager
import moe.nea.firmament.features.inventory.buttons.InventoryButtons
import moe.nea.firmament.features.inventory.storageoverlay.StorageOverlayScreen
import moe.nea.firmament.features.world.FairySouls
import moe.nea.firmament.gui.config.AllConfigsGui
import moe.nea.firmament.gui.config.BooleanHandler
import moe.nea.firmament.gui.config.ManagedOption
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
        thenLiteral("toggle") {
            thenArgument("config", string()) { config ->
                suggestsList {
                    AllConfigsGui.allConfigs.asSequence().map { it.name }.asIterable()
                }
                thenArgument("property", string()) { property ->
                    suggestsList {
                        (AllConfigsGui.allConfigs.find { it.name == this[config] } ?: return@suggestsList listOf())
                            .allOptions.entries.asSequence().filter { it.value.handler is BooleanHandler }
                            .map { it.key }
                            .asIterable()
                    }
                    thenExecute {
                        val config = this[config]
                        val property = this[property]

                        val configObj = AllConfigsGui.allConfigs.find { it.name == config }
                        if (configObj == null) {
                            source.sendFeedback(
                                Text.stringifiedTranslatable(
                                    "firmament.command.toggle.no-config-found",
                                    config
                                )
                            )
                            return@thenExecute
                        }
                        val propertyObj = configObj.allOptions[property]
                        if (propertyObj == null) {
                            source.sendFeedback(
                                Text.stringifiedTranslatable("firmament.command.toggle.no-property-found", property)
                            )
                            return@thenExecute
                        }
                        if (propertyObj.handler !is BooleanHandler) {
                            source.sendFeedback(
                                Text.stringifiedTranslatable("firmament.command.toggle.not-a-toggle", property)
                            )
                            return@thenExecute
                        }
                        propertyObj as ManagedOption<Boolean>
                        propertyObj.value = !propertyObj.value
                        configObj.save()
                        source.sendFeedback(
                            Text.stringifiedTranslatable(
                                "firmament.command.toggle.toggled", configObj.labelText,
                                propertyObj.labelText,
                                Text.translatable("firmament.toggle.${propertyObj.value}")
                            )
                        )
                    }
                }
            }
        }
    }
    thenLiteral("buttons") {
        thenExecute {
            InventoryButtons.openEditor()
        }
    }
    thenLiteral("sendcoords") {
        thenExecute {
            val p = MC.player ?: return@thenExecute
            MC.sendServerChat("x: ${p.blockX}, y: ${p.blockY}, z: ${p.blockZ}")
        }
        thenArgument("rest", RestArgumentType) { rest ->
            thenExecute {
                val p = MC.player ?: return@thenExecute
                MC.sendServerChat("x: ${p.blockX}, y: ${p.blockY}, z: ${p.blockZ} ${this[rest]}")
            }
        }
    }
    thenLiteral("storage") {
        thenExecute {
            ScreenUtil.setScreenLater(StorageOverlayScreen())
            MC.player?.networkHandler?.sendChatCommand("storage")
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
                source.sendFeedback(Text.stringifiedTranslatable("firmament.price", itemName.neuItem))
                val bazaarData = HypixelStaticData.bazaarData[itemName]
                if (bazaarData != null) {
                    source.sendFeedback(Text.translatable("firmament.price.bazaar"))
                    source.sendFeedback(
                        Text.stringifiedTranslatable("firmament.price.bazaar.productid", bazaarData.productId.bazaarId)
                    )
                    source.sendFeedback(
                        Text.stringifiedTranslatable(
                            "firmament.price.bazaar.buy.price",
                            FirmFormatters.formatCurrency(bazaarData.quickStatus.buyPrice, 1)
                        )
                    )
                    source.sendFeedback(
                        Text.stringifiedTranslatable(
                            "firmament.price.bazaar.buy.order",
                            bazaarData.quickStatus.buyOrders
                        )
                    )
                    source.sendFeedback(
                        Text.stringifiedTranslatable(
                            "firmament.price.bazaar.sell.price",
                            FirmFormatters.formatCurrency(bazaarData.quickStatus.sellPrice, 1)
                        )
                    )
                    source.sendFeedback(
                        Text.stringifiedTranslatable(
                            "firmament.price.bazaar.sell.order",
                            bazaarData.quickStatus.sellOrders
                        )
                    )
                }
                val lowestBin = HypixelStaticData.lowestBin[itemName]
                if (lowestBin != null) {
                    source.sendFeedback(
                        Text.stringifiedTranslatable(
                            "firmament.price.lowestbin",
                            FirmFormatters.formatCurrency(lowestBin, 1)
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
                source.sendFeedback(Text.stringifiedTranslatable("firmament.sbinfo.profile", SBData.profileId))
                val locrawInfo = SBData.locraw
                if (locrawInfo == null) {
                    source.sendFeedback(Text.translatable("firmament.sbinfo.nolocraw"))
                } else {
                    source.sendFeedback(Text.stringifiedTranslatable("firmament.sbinfo.server", locrawInfo.server))
                    source.sendFeedback(Text.stringifiedTranslatable("firmament.sbinfo.gametype", locrawInfo.gametype))
                    source.sendFeedback(Text.stringifiedTranslatable("firmament.sbinfo.mode", locrawInfo.mode))
                    source.sendFeedback(Text.stringifiedTranslatable("firmament.sbinfo.map", locrawInfo.map))
                }
            }
        }
        thenLiteral("callUrsa") {
            thenArgument("path", string()) { path ->
                thenExecute {
                    source.sendFeedback(Text.translatable("firmament.ursa.debugrequest.start"))
                    val text = UrsaManager.request(this[path].split("/")).bodyAsText()
                    source.sendFeedback(Text.stringifiedTranslatable("firmament.ursa.debugrequest.result", text))
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




