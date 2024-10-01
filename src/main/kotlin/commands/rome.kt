package moe.nea.firmament.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType.string
import io.ktor.client.statement.bodyAsText
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.nbt.NbtOps
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs
import moe.nea.firmament.apis.UrsaManager
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.FirmamentEventBus
import moe.nea.firmament.features.debug.DebugLogger
import moe.nea.firmament.features.debug.PowerUserTools
import moe.nea.firmament.features.inventory.buttons.InventoryButtons
import moe.nea.firmament.features.inventory.storageoverlay.StorageOverlayScreen
import moe.nea.firmament.features.inventory.storageoverlay.StorageOverviewScreen
import moe.nea.firmament.gui.config.AllConfigsGui
import moe.nea.firmament.gui.config.BooleanHandler
import moe.nea.firmament.gui.config.ManagedOption
import moe.nea.firmament.init.MixinPlugin
import moe.nea.firmament.repo.HypixelStaticData
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.ScreenUtil
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.accessors.messages
import moe.nea.firmament.util.collections.InstanceList
import moe.nea.firmament.util.collections.WeakCache


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
	thenLiteral("storageoverview") {
		thenExecute {
			ScreenUtil.setScreenLater(StorageOverviewScreen())
			MC.player?.networkHandler?.sendChatCommand("storage")
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
							FirmFormatters.formatCommas(bazaarData.quickStatus.buyPrice, 1)
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
							FirmFormatters.formatCommas(bazaarData.quickStatus.sellPrice, 1)
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
							FirmFormatters.formatCommas(lowestBin, 1)
						)
					)
				}
			}
		}
	}
	thenLiteral("dev") {
		thenLiteral("simulate") {
			thenArgument("message", RestArgumentType) { message ->
				thenExecute {
					MC.instance.messageHandler.onGameMessage(Text.literal(get(message)), false)
				}
			}
		}
		thenLiteral("debuglog") {
			thenLiteral("toggle") {
				thenArgument("tag", string()) { tag ->
					suggestsList { DebugLogger.allInstances.getAll().map { it.tag } + DebugLogger.EnabledLogs.data }
					thenExecute {
						val tagText = this[tag]
						val enabled = DebugLogger.EnabledLogs.data
						if (tagText in enabled) {
							enabled.remove(tagText)
							source.sendFeedback(Text.literal("Disabled $tagText debug logging"))
						} else {
							enabled.add(tagText)
							source.sendFeedback(Text.literal("Enabled $tagText debug logging"))
						}
					}
				}
			}
		}
		thenLiteral("dumpchat") {
			thenExecute {
				MC.inGameHud.chatHud.messages.forEach {
					val nbt = TextCodecs.CODEC.encodeStart(NbtOps.INSTANCE, it.content).orThrow
					println(nbt)
				}
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
		thenLiteral("copyEntities") {
			thenExecute {
				val player = MC.player ?: return@thenExecute
				player.world.getOtherEntities(player, player.boundingBox.expand(12.0))
					.forEach(PowerUserTools::showEntity)
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
		thenLiteral("events") {
			thenExecute {
				source.sendFeedback(Text.translatable("firmament.event.start"))
				FirmamentEventBus.allEventBuses.forEach { eventBus ->
					source.sendFeedback(Text.translatable(
						"firmament.event.bustype",
						eventBus.eventType.typeName.removePrefix("moe.nea.firmament")))
					eventBus.handlers.forEach { handler ->
						source.sendFeedback(Text.translatable(
							"firmament.event.handler",
							handler.label))
					}
				}
			}
		}
		thenLiteral("caches") {
			thenExecute {
				source.sendFeedback(Text.literal("Caches:"))
				WeakCache.allInstances.getAll().forEach {
					source.sendFeedback(Text.literal(" - ${it.name}: ${it.size}"))
				}
				source.sendFeedback(Text.translatable("Instance lists:"))
				InstanceList.allInstances.getAll().forEach {
					source.sendFeedback(Text.literal(" - ${it.name}: ${it.size}"))
				}
			}
		}
		thenLiteral("mixins") {
			thenExecute {
				source.sendFeedback(Text.translatable("firmament.mixins.start"))
				MixinPlugin.appliedMixins
					.map { it.removePrefix(MixinPlugin.mixinPackage) }
					.forEach {
						source.sendFeedback(Text.literal(" - ").withColor(0xD020F0)
							                    .append(Text.literal(it).withColor(0xF6BA20)))
					}
			}
		}
	}
	CommandEvent.SubCommand.publish(CommandEvent.SubCommand(this@literal))
}


fun registerFirmamentCommand(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
	val firmament = dispatcher.register(firmamentCommand())
	dispatcher.register(literal("firm") {
		redirect(firmament)
	})
}




