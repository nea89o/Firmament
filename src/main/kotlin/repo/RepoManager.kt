package moe.nea.firmament.repo

import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.NEURepositoryException
import io.github.moulberry.repo.data.NEUItem
import io.github.moulberry.repo.data.NEURecipe
import io.github.moulberry.repo.data.Rarity
import java.nio.file.Path
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import kotlinx.coroutines.launch
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket
import net.minecraft.recipe.display.CuttingRecipeDisplay
import moe.nea.firmament.Firmament
import moe.nea.firmament.Firmament.logger
import moe.nea.firmament.events.ReloadRegistrationEvent
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.MinecraftDispatcher
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.TestUtil
import moe.nea.firmament.util.tr

object RepoManager {
	object Config : ManagedConfig("repo", Category.META) {
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

		val disableItemGroups by toggle("disable-item-groups") { true }
		val reload by button("reload") {
			save()
			RepoManager.reload()
		}
		val redownload by button("redownload") {
			save()
			RepoManager.launchAsyncUpdate(true)
		}
		val alwaysSuperCraft by toggle("enable-super-craft") { true }
		var warnForMissingItemListMod by toggle("warn-for-missing-item-list-mod") { true }
	}

	val currentDownloadedSha by RepoDownloadManager::latestSavedVersionHash

	var recentlyFailedToUpdateItemList = false

	val essenceRecipeProvider = EssenceRecipeProvider()
	val recipeCache = BetterRepoRecipeCache(essenceRecipeProvider)

	fun makeNEURepository(path: Path): NEURepository {
		return NEURepository.of(path).apply {
			registerReloadListener(ItemCache)
			registerReloadListener(ExpLadders)
			registerReloadListener(ItemNameLookup)
			ReloadRegistrationEvent.publish(ReloadRegistrationEvent(this))
			registerReloadListener {
				if (TestUtil.isInTest) return@registerReloadListener
				Firmament.coroutineScope.launch(MinecraftDispatcher) {
					if (!trySendClientboundUpdateRecipesPacket()) {
						logger.warn("Failed to issue a ClientboundUpdateRecipesPacket (to reload REI). This may lead to an outdated item list.")
						recentlyFailedToUpdateItemList = true
					}
				}
			}
			registerReloadListener(essenceRecipeProvider)
			registerReloadListener(recipeCache)
		}
	}

	lateinit var neuRepo: NEURepository
		private set

	fun getAllRecipes() = neuRepo.items.items.values.asSequence().flatMap { it.recipes }

	fun getRecipesFor(skyblockId: SkyblockId): Set<NEURecipe> = recipeCache.recipes[skyblockId] ?: setOf()
	fun getUsagesFor(skyblockId: SkyblockId): Set<NEURecipe> = recipeCache.usages[skyblockId] ?: setOf()

	private fun trySendClientboundUpdateRecipesPacket(): Boolean {
		return MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().networkHandler?.onSynchronizeRecipes(
			SynchronizeRecipesS2CPacket(mutableMapOf(), CuttingRecipeDisplay.Grouping.empty())
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
			ItemCache.ReloadProgressHud.reportProgress("Downloading", 0, -1) // TODO: replace with a proper bouncy bar
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

	fun reloadForTest(from: Path) {
		neuRepo = makeNEURepository(from)
		reload()
	}

	fun reload() {
		if (!TestUtil.isInTest && !MC.instance.isOnThread) {
			MC.instance.send {
				reload()
			}
			return
		}
		try {
			ItemCache.ReloadProgressHud.reportProgress("Reloading from Disk",
			                                           0,
			                                           -1) // TODO: replace with a proper bouncy bar
			ItemCache.ReloadProgressHud.isEnabled = true
			logger.info("Repo reload started.")
			neuRepo.reload()
			logger.info("Repo reload completed.")
		} catch (exc: NEURepositoryException) {
			ErrorUtil.softError("Failed to reload repository", exc)
			MC.sendChat(
				tr("firmament.repo.reloadfail",
				   "Failed to reload repository. This will result in some mod features not working.")
			)
			ItemCache.ReloadProgressHud.isEnabled = false
		}
	}

	private var wasInitialized = false
	fun initialize() {
		if (wasInitialized) return
		wasInitialized = true
		System.getProperty("firmament.testrepo")?.let { compTimeRepo ->
			reloadForTest(Path.of(compTimeRepo))
			return
		}
		neuRepo = makeNEURepository(RepoDownloadManager.repoSavedLocation)
		if (Config.autoUpdate) {
			launchAsyncUpdate()
		} else {
			reload()
		}
	}

	init {
		if (TestUtil.isInTest) {
			initialize()
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
		if (intIndex !in Rarity.entries.indices) return null
		if (petId !in neuRepo.constants.petNumbers) return null
		return PetData(Rarity.entries[intIndex], petId, 0.0, true)
	}

	fun getRepoRef(): String {
		return "${Config.username}/${Config.reponame}#${Config.branch}"
	}
}
