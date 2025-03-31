package moe.nea.firmament.features.inventory

import java.net.URI
import net.fabricmc.loader.api.FabricLoader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import net.minecraft.SharedConstants
import net.minecraft.text.ClickEvent
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.SkyblockServerUpdateEvent
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.aqua
import moe.nea.firmament.util.bold
import moe.nea.firmament.util.clickCommand
import moe.nea.firmament.util.grey
import moe.nea.firmament.util.lime
import moe.nea.firmament.util.red
import moe.nea.firmament.util.white
import moe.nea.firmament.util.yellow

object REIDependencyWarner {
	val reiModId = "roughlyenoughitems"
	val hasREI = FabricLoader.getInstance().isModLoaded(reiModId)
	var sentWarning = false

	fun modrinthLink(slug: String) =
		"https://modrinth.com/mod/$slug/versions?g=${SharedConstants.getGameVersion().name}&l=fabric"

	fun downloadButton(modName: String, modId: String, slug: String): Text {
		val alreadyDownloaded = FabricLoader.getInstance().isModLoaded(modId)
		return Text.literal(" - ")
			.white()
			.append(Text.literal("[").aqua())
			.append(Text.translatable("firmament.download", modName)
				        .styled { it.withClickEvent(ClickEvent.OpenUrl(URI (modrinthLink(slug)))) }
				        .yellow()
				        .also {
					        if (alreadyDownloaded)
						        it.append(Text.translatable("firmament.download.already", modName)
							                  .lime())
				        })
			.append(Text.literal("]").aqua())
	}

	@Subscribe
	fun checkREIDependency(event: SkyblockServerUpdateEvent) {
		if (!SBData.isOnSkyblock) return
		if (hasREI) return
		if (sentWarning) return
		sentWarning = true
		Firmament.coroutineScope.launch {
			delay(2.seconds)
			// TODO: should we offer an automatic install that actually downloads the JARs and places them into the mod folder?
			MC.sendChat(
				Text.translatable("firmament.reiwarning").red().bold().append("\n")
					.append(downloadButton("RoughlyEnoughItems", reiModId, "rei")).append("\n")
					.append(downloadButton("Architectury API", "architectury", "architectury-api")).append("\n")
					.append(downloadButton("Cloth Config API", "cloth-config", "cloth-config")).append("\n")
					.append(Text.translatable("firmament.reiwarning.disable")
						        .clickCommand("/firm disablereiwarning")
						        .grey())
			)
		}
	}

	@Subscribe
	fun onSubcommand(event: CommandEvent.SubCommand) {
		if (hasREI) return
		event.subcommand("disablereiwarning") {
			thenExecute {
				RepoManager.Config.warnForMissingItemListMod = false
				RepoManager.Config.save()
				MC.sendChat(Text.translatable("firmament.reiwarning.disabled").yellow())
			}
		}
	}
}
