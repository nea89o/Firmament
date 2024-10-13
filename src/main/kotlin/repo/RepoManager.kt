package moe.nea.firmament.repo

import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.NEURepositoryException
import io.github.moulberry.repo.data.NEUItem
import io.github.moulberry.repo.data.NEURecipe
import io.github.moulberry.repo.data.Rarity
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import kotlinx.coroutines.launch
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.Firmament.logger
import moe.nea.firmament.events.ReloadRegistrationEvent
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.rei.PetData
import moe.nea.firmament.util.MinecraftDispatcher
import moe.nea.firmament.util.SkyblockId

object RepoManager {
	object Config : ManagedConfig("repo") {
		var username by string("username") { "NotEnoughUpdates" }
		var reponame by string("reponame") { "NotEnoughUpdates-REPO" }
		var branch by string("branch") { "master" }
		val autoUpdate by toggle("autoUpdate") { true }
		val reset by button("reset") {
			username = "NotEnoughUpdates"
			reponame = "NotEnoughUpdates-REPO"
			branch = "master"
			save()
		}

		val enableYacl by toggle("enable-yacl") { false }

		val disableItemGroups by toggle("disable-item-groups") { true }
		val reload by button("reload") {
			save()
			RepoManager.reload()
		}
		val redownload by button("redownload") {
			save()
			RepoManager.launchAsyncUpdate(true)
		}
	}

	val currentDownloadedSha by RepoDownloadManager::latestSavedVersionHash

	var recentlyFailedToUpdateItemList = false

	val neuRepo: NEURepository = NEURepository.of(RepoDownloadManager.repoSavedLocation).apply {
		registerReloadListener(ItemCache)
		registerReloadListener(ExpLadders)
		registerReloadListener(ItemNameLookup)
		ReloadRegistrationEvent.publish(ReloadRegistrationEvent(this))
		registerReloadListener {
			Firmament.coroutineScope.launch(MinecraftDispatcher) {
				if (!trySendClientboundUpdateRecipesPacket()) {
					logger.warn("Failed to issue a ClientboundUpdateRecipesPacket (to reload REI). This may lead to an outdated item list.")
					recentlyFailedToUpdateItemList = true
				}
			}
		}
	}

	val essenceRecipeProvider = EssenceRecipeProvider()
	val recipeCache = BetterRepoRecipeCache(essenceRecipeProvider)

	init {
		neuRepo.registerReloadListener(essenceRecipeProvider)
		neuRepo.registerReloadListener(recipeCache)
	}

	fun getAllRecipes() = neuRepo.items.items.values.asSequence().flatMap { it.recipes }

	fun getRecipesFor(skyblockId: SkyblockId): Set<NEURecipe> = recipeCache.recipes[skyblockId] ?: setOf()
	fun getUsagesFor(skyblockId: SkyblockId): Set<NEURecipe> = recipeCache.usages[skyblockId] ?: setOf()

	private fun trySendClientboundUpdateRecipesPacket(): Boolean {
		return MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().networkHandler?.onSynchronizeRecipes(
			SynchronizeRecipesS2CPacket(mutableListOf())
		) != null
	}

	init {
		ClientTickEvents.START_WORLD_TICK.register(ClientTickEvents.StartWorldTick {
			if (recentlyFailedToUpdateItemList && trySendClientboundUpdateRecipesPacket())
				recentlyFailedToUpdateItemList = false
		})
	}

	fun getNEUItem(skyblockId: SkyblockId): NEUItem? = neuRepo.items.getItemBySkyblockId(skyblockId.neuItem)

	fun launchAsyncUpdate(force: Boolean = false) {
		Firmament.coroutineScope.launch {
			ItemCache.ReloadProgressHud.reportProgress("Downloading", 0, -1) // TODO: replace with a proper boundy bar
			ItemCache.ReloadProgressHud.isEnabled = true
			try {
				RepoDownloadManager.downloadUpdate(force)
				ItemCache.ReloadProgressHud.reportProgress("Download complete", 1, 1)
			} finally {
				ItemCache.ReloadProgressHud.isEnabled = false
			}
			reload()
		}
	}

	fun reload() {
		try {
			ItemCache.ReloadProgressHud.reportProgress("Reloading from Disk",
			                                           0,
			                                           -1) // TODO: replace with a proper boundy bar
			ItemCache.ReloadProgressHud.isEnabled = true
			neuRepo.reload()
		} catch (exc: NEURepositoryException) {
			MinecraftClient.getInstance().player?.sendMessage(
				Text.literal("Failed to reload repository. This will result in some mod features not working.")
			)
			ItemCache.ReloadProgressHud.isEnabled = false
			exc.printStackTrace()
		}
	}

	fun initialize() {
		if (Config.autoUpdate) {
			launchAsyncUpdate()
		} else {
			reload()
		}
	}

	fun getPotentialStubPetData(skyblockId: SkyblockId): PetData? {
		val parts = skyblockId.neuItem.split(";")
		if (parts.size != 2) {
			return null
		}
		val (petId, rarityIndex) = parts
		if (!rarityIndex.all { it.isDigit() }) {
			return null
		}
		val intIndex = rarityIndex.toInt()
		if (intIndex !in Rarity.values().indices) return null
		if (petId !in neuRepo.constants.petNumbers) return null
		return PetData(Rarity.values()[intIndex], petId, 0.0, true)
	}

}
