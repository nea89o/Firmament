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

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.Text
import moe.nea.firmament.features.world.FairySouls
import moe.nea.firmament.gui.config.AllConfigsGui
import moe.nea.firmament.repo.ItemCostData
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.SkyblockId


fun firmamentCommand() = literal("firmament") {
    thenLiteral("config") {
        thenExecute {
            AllConfigsGui.showAllGuis()
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
    thenLiteral("price") {
        thenArgument("item", string()) { item ->
            suggestsList { RepoManager.neuRepo.items.items.keys }
            thenExecute {
                val itemName = SkyblockId(getString(context, "item"))
                source.sendFeedback(Text.translatable("firmament.price", itemName.neuItem))
                val bazaarData = ItemCostData.bazaarData[itemName]
                if (bazaarData != null) {
                    source.sendFeedback(Text.translatable("firmament.price.bazaar"))
                    source.sendFeedback(Text.translatable("firmament.price.bazaar.productid", bazaarData.productId.bazaarId))
                    source.sendFeedback(Text.translatable("firmament.price.bazaar.buy.price", bazaarData.quickStatus.buyPrice))
                    source.sendFeedback(Text.translatable("firmament.price.bazaar.buy.order", bazaarData.quickStatus.buyOrders))
                    source.sendFeedback(Text.translatable("firmament.price.bazaar.sell.price", bazaarData.quickStatus.sellPrice))
                    source.sendFeedback(Text.translatable("firmament.price.bazaar.sell.order", bazaarData.quickStatus.sellOrders))
                }
                val lowestBin = ItemCostData.lowestBin[itemName]
                if (lowestBin != null) {
                    source.sendFeedback(Text.translatable("firmament.price.lowestbin", lowestBin))
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
                source.sendFeedback(Text.translatable("firmament.sbinfo.profile", SBData.profileCuteName))
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




