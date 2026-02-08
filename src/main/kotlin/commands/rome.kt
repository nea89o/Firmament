package moe.nea.firmament.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType.string
import java.net.http.HttpResponse
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import kotlinx.coroutines.launch
import net.minecraft.commands.CommandBuildContext
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import moe.nea.firmament.Firmament
import moe.nea.firmament.apis.UrsaManager
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.FirmamentEventBus
import moe.nea.firmament.features.debug.DebugLogger
import moe.nea.firmament.features.debug.DeveloperFeatures
import moe.nea.firmament.features.debug.PowerUserTools
import moe.nea.firmament.features.inventory.buttons.InventoryButtons
import moe.nea.firmament.features.inventory.storageoverlay.StorageOverlayScreen
import moe.nea.firmament.features.inventory.storageoverlay.StorageOverviewScreen
import moe.nea.firmament.features.mining.MiningBlockInfoUi
import moe.nea.firmament.gui.config.AllConfigsGui
import moe.nea.firmament.gui.config.BooleanHandler
import moe.nea.firmament.gui.config.ManagedOption
import moe.nea.firmament.init.MixinPlugin
import moe.nea.firmament.repo.HypixelStaticData
import moe.nea.firmament.repo.ItemCache
import moe.nea.firmament.repo.RepoDownloadManager
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.FirmFormatters.debugPath
import moe.nea.firmament.util.FirmFormatters.formatBool
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.ScreenUtil
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.accessors.messages
import moe.nea.firmament.util.asBazaarStock
import moe.nea.firmament.util.collections.InstanceList
import moe.nea.firmament.util.collections.WeakCache
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.mc.SNbtFormatter
import moe.nea.firmament.util.tr
import moe.nea.firmament.util.unformattedString


fun firmamentCommand(ctx: CommandBuildContext) = literal("firmament") {
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
								Component.translatableEscape(
									"firmament.command.toggle.no-config-found",
									config
								)
							)
							return@thenExecute
						}
						val propertyObj = configObj.allOptions[property]
						if (propertyObj == null) {
							source.sendFeedback(
								Component.translatableEscape("firmament.command.toggle.no-property-found", property)
							)
							return@thenExecute
						}
						if (propertyObj.handler !is BooleanHandler) {
							source.sendFeedback(
								Component.translatableEscape("firmament.command.toggle.not-a-toggle", property)
							)
							return@thenExecute
						}
						propertyObj as ManagedOption<Boolean>
						propertyObj.value = !propertyObj.value
						configObj.markDirty()
						source.sendFeedback(
							Component.translatableEscape(
								"firmament.command.toggle.toggled", configObj.labelText,
								propertyObj.labelText,
								Component.translatable("firmament.toggle.${propertyObj.value}")
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
			MC.player?.connection?.sendCommand("storage")
		}
	}
	thenLiteral("storage") {
		thenExecute {
			ScreenUtil.setScreenLater(StorageOverlayScreen())
			MC.player?.connection?.sendCommand("storage")
		}
	}
	thenLiteral("repo") {
		thenLiteral("checkpr") {
			thenArgument("prnum", IntegerArgumentType.integer(1)) { prnum ->
				thenExecute {
					val prnum = this[prnum]
					source.sendFeedback(tr("firmament.repo.reload.pr", "Temporarily reloading repo from PR #${prnum}."))
					RepoManager.downloadOverridenBranch("refs/pull/$prnum/head")
				}
			}
		}
		thenLiteral("reload") {
			thenLiteral("fetch") {
				thenExecute {
					source.sendFeedback(Component.translatable("firmament.repo.reload.network")) // TODO better reporting
					RepoManager.launchAsyncUpdate()
				}
			}
			thenExecute {
				source.sendFeedback(Component.translatable("firmament.repo.reload.disk"))
				Firmament.coroutineScope.launch { RepoManager.reload() }
			}
		}
	}
	thenLiteral("price") {
		thenArgument("item", string()) { item ->
			suggestsList { RepoManager.neuRepo.items.items.keys }
			thenExecute {
				val itemName = SkyblockId(get(item))
				source.sendFeedback(Component.translatableEscape("firmament.price", itemName.neuItem))
				val bazaarData = HypixelStaticData.bazaarData[itemName.asBazaarStock]
				if (bazaarData != null) {
					source.sendFeedback(Component.translatable("firmament.price.bazaar"))
					source.sendFeedback(
						Component.translatableEscape("firmament.price.bazaar.productid", bazaarData.productId.bazaarId)
					)
					source.sendFeedback(
						Component.translatableEscape(
							"firmament.price.bazaar.buy.price",
							FirmFormatters.formatCommas(bazaarData.quickStatus.buyPrice, 1)
						)
					)
					source.sendFeedback(
						Component.translatableEscape(
							"firmament.price.bazaar.buy.order",
							bazaarData.quickStatus.buyOrders
						)
					)
					source.sendFeedback(
						Component.translatableEscape(
							"firmament.price.bazaar.sell.price",
							FirmFormatters.formatCommas(bazaarData.quickStatus.sellPrice, 1)
						)
					)
					source.sendFeedback(
						Component.translatableEscape(
							"firmament.price.bazaar.sell.order",
							bazaarData.quickStatus.sellOrders
						)
					)
				}
				val lowestBin = HypixelStaticData.lowestBin[itemName]
				if (lowestBin != null) {
					source.sendFeedback(
						Component.translatableEscape(
							"firmament.price.lowestbin",
							FirmFormatters.formatCommas(lowestBin, 1)
						)
					)
				}
			}
		}
	}
	thenLiteral(DeveloperFeatures.DEVELOPER_SUBCOMMAND) {
		thenLiteral("simulate") {
			thenArgument("message", RestArgumentType) { message ->
				thenExecute {
					MC.instance.chatListener.handleSystemMessage(Component.literal(get(message)), false)
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
							source.sendFeedback(Component.literal("Disabled $tagText debug logging"))
						} else {
							enabled.add(tagText)
							source.sendFeedback(Component.literal("Enabled $tagText debug logging"))
						}
					}
				}
			}
		}
		thenLiteral("screens") {
			thenExecute {
				MC.sendChat(
					Component.literal(
						"""
					|Screen: ${MC.screen} (${MC.screen?.title})
					|Screen Handler: ${MC.handledScreen?.menu} ${MC.handledScreen?.menu?.containerId}
					|Player Screen Handler: ${MC.player?.containerMenu} ${MC.player?.containerMenu?.containerId}
				""".trimMargin()
					)
				)
			}
		}
		thenLiteral("blocks") {
			thenExecute {
				ScreenUtil.setScreenLater(MiningBlockInfoUi.makeScreen())
			}
		}
		thenLiteral("dumpchat") {
			thenExecute {
				MC.inGameHud.chat.messages.forEach {
					val nbt = ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, it.content).orThrow
					println(nbt)
				}
			}
			thenArgument("search", string()) { search ->
				thenExecute {
					MC.inGameHud.chat.messages
						.filter { this[search] in it.content.unformattedString }
						.forEach {
							val nbt = ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, it.content).orThrow
							println(SNbtFormatter.prettify(nbt))
						}
				}
			}
		}
		thenLiteral("sbdata") {
			thenExecute {
				source.sendFeedback(Component.translatableEscape("firmament.sbinfo.profile", SBData.profileId ?: "null"))
				val locrawInfo = SBData.locraw
				if (locrawInfo == null) {
					source.sendFeedback(Component.translatable("firmament.sbinfo.nolocraw"))
				} else {
					source.sendFeedback(Component.translatableEscape("firmament.sbinfo.server", locrawInfo.server ?: "null"))
					source.sendFeedback(Component.translatableEscape("firmament.sbinfo.gametype", locrawInfo.gametype ?: "null"))
					source.sendFeedback(Component.translatableEscape("firmament.sbinfo.mode", locrawInfo.mode ?: "null"))
					source.sendFeedback(Component.translatableEscape("firmament.sbinfo.map", locrawInfo.map ?: "null"))
					source.sendFeedback(
						tr(
							"firmament.sbinfo.custommining",
							"Custom Mining: ${formatBool(locrawInfo.skyblockLocation?.hasCustomMining ?: false)}"
						)
					)
				}
			}
		}
		thenLiteral("copyEntities") {
			thenExecute {
				val player = MC.player ?: return@thenExecute
				player.level.getEntities(player, player.boundingBox.inflate(12.0))
					.forEach(PowerUserTools::showEntity)
				PowerUserTools.showEntity(player)
			}
		}
		thenLiteral("callUrsa") {
			thenArgument("path", string()) { path ->
				thenExecute {
					Firmament.coroutineScope.launch {
						source.sendFeedback(Component.translatable("firmament.ursa.debugrequest.start"))
						val text = UrsaManager.request(get(path).split("/"), HttpResponse.BodyHandlers.ofString())
						source.sendFeedback(Component.translatableEscape("firmament.ursa.debugrequest.result", text))
					}
				}
			}
		}
		thenLiteral("events") {
			thenExecute {
				source.sendFeedback(tr("firmament.event.start", "Event Bus Readout:"))
				FirmamentEventBus.allEventBuses.forEach { eventBus ->
					val prefixName = eventBus.eventType.typeName.removePrefix("moe.nea.firmament")
					source.sendFeedback(
						tr(
							"firmament.event.bustype",
							"- $prefixName:"
						)
					)
					eventBus.handlers.forEach { handler ->
						source.sendFeedback(
							tr(
								"firmament.event.handler",
								"   * ${handler.label}"
							)
						)
					}
				}
			}
		}
		thenLiteral("caches") {
			thenExecute {
				source.sendFeedback(Component.literal("Caches:"))
				WeakCache.allInstances.getAll().forEach {
					source.sendFeedback(Component.literal(" - ${it.name}: ${it.size}"))
				}
				source.sendFeedback(Component.translatable("Instance lists:"))
				InstanceList.allInstances.getAll().forEach {
					source.sendFeedback(Component.literal(" - ${it.name}: ${it.size}"))
				}
			}
		}
		thenLiteral("mixins") {
			thenExecute {
				MixinPlugin.instances.forEach { plugin ->
					source.sendFeedback(tr("firmament.mixins.start.package", "Mixins (base ${plugin.mixinPackage}):"))
					plugin.appliedMixins
						.map { it.removePrefix(plugin.mixinPackage) }
						.forEach {
							source.sendFeedback(
								Component.literal(" - ").withColor(0xD020F0)
									.append(Component.literal(it).withColor(0xF6BA20))
							)
						}
				}
			}
		}
		thenLiteral("repo") {
			thenExecute {
				source.sendFeedback(tr("firmament.repo.info.ref", "Repo Upstream: ${RepoManager.getRepoRef()}"))
				source.sendFeedback(
					tr(
						"firmament.repo.info.downloadedref",
						"Downloaded ref: ${RepoDownloadManager.latestSavedVersionHash}"
					)
				)
				source.sendFeedback(
					tr(
						"firmament.repo.info.location",
						"Saved location: ${debugPath(RepoDownloadManager.repoSavedLocation)}"
					)
				)
				source.sendFeedback(
					tr(
						"firmament.repo.info.reloadstatus",
						"Incomplete: ${
							formatBool(
								RepoManager.neuRepo.isIncomplete,
								trueIsGood = false
							)
						}, Unstable ${formatBool(RepoManager.neuRepo.isUnstable, trueIsGood = false)}"
					)
				)
				source.sendFeedback(
					tr(
						"firmament.repo.info.items",
						"Loaded items: ${RepoManager.neuRepo.items?.items?.size}"
					)
				)
				source.sendFeedback(
					tr(
						"firmament.repo.info.overlays",
						"Overlays: ${RepoManager.overlayData.overlays.size}"
					)
				)
				source.sendFeedback(
					tr(
						"firmament.repo.info.itemcache",
						"ItemCache flawless: ${formatBool(ItemCache.isFlawless)}"
					)
				)
				source.sendFeedback(
					tr(
						"firmament.repo.info.itemdir",
						"Items on disk: ${debugPath(RepoDownloadManager.repoSavedLocation.resolve("items"))}"
					)
				)
			}
		}
	}
	thenExecute {
		AllConfigsGui.showAllGuis()
	}
	CommandEvent.SubCommand.publish(CommandEvent.SubCommand(this@literal, ctx))
}


fun registerFirmamentCommand(dispatcher: CommandDispatcher<FabricClientCommandSource>, ctx: CommandBuildContext) {
	val firmament = dispatcher.register(firmamentCommand(ctx))
	dispatcher.register(literal("firm") {
		redirect(firmament)
	})
}




