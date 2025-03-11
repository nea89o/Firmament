package moe.nea.firmament.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType.string
import io.ktor.client.statement.bodyAsText
import java.util.ServiceLoader
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.item.ItemStack
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
import moe.nea.firmament.features.mining.MiningBlockInfoUi
import moe.nea.firmament.gui.config.AllConfigsGui
import moe.nea.firmament.gui.config.BooleanHandler
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.gui.config.ManagedOption
import moe.nea.firmament.init.MixinPlugin
import moe.nea.firmament.repo.HypixelStaticData
import moe.nea.firmament.repo.ItemCache
import moe.nea.firmament.repo.RepoDownloadManager
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.repo.item.SBItemId
import moe.nea.firmament.repo.item.SBItemData
import moe.nea.firmament.repo.item.SBItemProperty
import moe.nea.firmament.repo.item.SBStackSize
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.FirmFormatters.debugPath
import moe.nea.firmament.util.FirmFormatters.formatBool
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.ScreenUtil
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.accessors.messages
import moe.nea.firmament.util.blue
import moe.nea.firmament.util.collections.InstanceList
import moe.nea.firmament.util.collections.WeakCache
import moe.nea.firmament.util.gold
import moe.nea.firmament.util.grey
import moe.nea.firmament.util.lime
import moe.nea.firmament.util.mc.SNbtFormatter
import moe.nea.firmament.util.mc.SNbtFormatter.Companion.toPrettyString
import moe.nea.firmament.util.red
import moe.nea.firmament.util.tr
import moe.nea.firmament.util.unformattedString


fun firmamentCommand() = literal("firmament") {
	thenLiteral("config") {
		thenExecute {
			AllConfigsGui.showAllGuis()
		}
		thenLiteral("toggle") {
			thenArgument("config", string()) { config ->
				suggestsList {
					ManagedConfig.allManagedConfigs.getAll().asSequence().map { it.name }.asIterable()
				}
				thenArgument("property", string()) { property ->
					suggestsList {
						(ManagedConfig.allManagedConfigs.getAll().find { it.name == this[config] }
							?: return@suggestsList listOf())
							.allOptions.entries.asSequence().filter { it.value.handler is BooleanHandler }
							.map { it.key }
							.asIterable()
					}
					thenExecute {
						val config = this[config]
						val property = this[property]

						val configObj = ManagedConfig.allManagedConfigs.getAll().find { it.name == config }
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
		thenLiteral("blocks") {
			thenExecute {
				ScreenUtil.setScreenLater(MiningBlockInfoUi.makeScreen())
			}
		}
		thenLiteral("scratch") {
			thenExecute {
				val original = SBItemData.fromStack(MC.stackInHand)
				original.debugStackCreation().forEach {
					println("============== Applied modifier: ${it.lastAppliedModifier} (${it.data}) ==============")
					if (it.stack.isEmpty)
						println("<empty>")
					else
						println(ItemStack.CODEC
							        .encodeStart(MC.currentOrDefaultNbtOps, it.stack)
							        .orThrow.toPrettyString())
				}
				println("============== FINISHED ==============")
				val roundtripped = original.roundtrip()
				fun <T> printProp(prop: SBItemProperty<T>) {
					val data = roundtripped.getData(prop)
					val oldData = original.getData(prop)
					val dataT = Text.literal("${data}")
					if (oldData == null)
						dataT.gold()
					else if (oldData == data)
						dataT.lime()
					else
						dataT.red()
					source.sendFeedback(Text.literal("${prop.javaClass.simpleName}")
						                    .blue()
						                    .append(Text.literal(": ").grey())
						                    .append(dataT))
				}
				SBItemProperty.allProperties.forEach { prop ->
					printProp(prop)
				}
				source.sendFeedback(tr("firmament.itemdebug.done", "Item reconstruction finished, check your console."))
			}
		}
		thenLiteral("dumpchat") {
			thenExecute {
				MC.inGameHud.chatHud.messages.forEach {
					val nbt = TextCodecs.CODEC.encodeStart(NbtOps.INSTANCE, it.content).orThrow
					println(nbt)
				}
			}
			thenArgument("search", string()) { search ->
				thenExecute {
					MC.inGameHud.chatHud.messages
						.filter { this[search] in it.content.unformattedString }
						.forEach {
							val nbt = TextCodecs.CODEC.encodeStart(NbtOps.INSTANCE, it.content).orThrow
							println(SNbtFormatter.prettify(nbt))
						}
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
					source.sendFeedback(tr("firmament.sbinfo.custommining",
					                       "Custom Mining: ${formatBool(locrawInfo.skyblockLocation?.hasCustomMining ?: false)}"))
				}
			}
		}
		thenLiteral("copyEntities") {
			thenExecute {
				val player = MC.player ?: return@thenExecute
				player.world.getOtherEntities(player, player.boundingBox.expand(12.0))
					.forEach(PowerUserTools::showEntity)
				PowerUserTools.showEntity(player)
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
				source.sendFeedback(tr("firmament.event.start", "Event Bus Readout:"))
				FirmamentEventBus.allEventBuses.forEach { eventBus ->
					val prefixName = eventBus.eventType.typeName.removePrefix("moe.nea.firmament")
					source.sendFeedback(tr(
						"firmament.event.bustype",
						"- $prefixName:"))
					eventBus.handlers.forEach { handler ->
						source.sendFeedback(tr(
							"firmament.event.handler",
							"   * ${handler.label}"))
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
		thenLiteral("repo") {
			thenExecute {
				source.sendFeedback(tr("firmament.repo.info.ref", "Repo Upstream: ${RepoManager.getRepoRef()}"))
				source.sendFeedback(tr("firmament.repo.info.downloadedref",
				                       "Downloaded ref: ${RepoDownloadManager.latestSavedVersionHash}"))
				source.sendFeedback(tr("firmament.repo.info.location",
				                       "Saved location: ${debugPath(RepoDownloadManager.repoSavedLocation)}"))
				source.sendFeedback(tr("firmament.repo.info.reloadstatus",
				                       "Incomplete: ${
					                       formatBool(RepoManager.neuRepo.isIncomplete,
					                                  trueIsGood = false)
				                       }, Unstable ${formatBool(RepoManager.neuRepo.isUnstable, trueIsGood = false)}"))
				source.sendFeedback(tr("firmament.repo.info.items",
				                       "Loaded items: ${RepoManager.neuRepo.items?.items?.size}"))
				source.sendFeedback(tr("firmament.repo.info.itemcache",
				                       "ItemCache flawless: ${formatBool(ItemCache.isFlawless)}"))
				source.sendFeedback(tr("firmament.repo.info.itemdir",
				                       "Items on disk: ${debugPath(RepoDownloadManager.repoSavedLocation.resolve("items"))}"))
			}
		}
	}
	thenExecute {
		AllConfigsGui.showAllGuis()
	}
	CommandEvent.SubCommand.publish(CommandEvent.SubCommand(this@literal))
}


fun registerFirmamentCommand(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
	val firmament = dispatcher.register(firmamentCommand())
	dispatcher.register(literal("firm") {
		redirect(firmament)
	})
}




