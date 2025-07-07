package moe.nea.firmament.repo

import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.NEURepositoryException
import io.github.moulberry.repo.data.NEUItem
import io.github.moulberry.repo.data.NEURecipe
import io.github.moulberry.repo.data.Rarity
import java.nio.file.Path
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket
import net.minecraft.recipe.display.CuttingRecipeDisplay
import net.minecraft.util.StringIdentifiable
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
		val enableREI by toggle("enable-rei") { true }
		val disableItemGroups by toggle("disable-item-groups") { true }
		val reload by button("reload") {
			save()
			Firmament.coroutineScope.launch {
				RepoManager.reload()
			}
		}
		val redownload by button("redownload") {
			save()
			RepoManager.launchAsyncUpdate(true)
		}
		val alwaysSuperCraft by toggle("enable-super-craft") { true }
		var warnForMissingItemListMod by toggle("warn-for-missing-item-list-mod") { true }
		val perfectRenders by choice("perfect-renders") { PerfectRender.RENDER }
	}

	enum class PerfectRender(val label: String) : StringIdentifiable {
		NOTHING("nothing"),
		RENDER("render"),
		RENDER_AND_TEXT("text"),
		;

		fun rendersPerfectText() = this == RENDER_AND_TEXT
		fun rendersPerfectVisuals() = this == RENDER || this == RENDER_AND_TEXT

		override fun asString(): String? = label
	}

	val currentDownloadedSha by RepoDownloadManager::latestSavedVersionHash

	var recentlyFailedToUpdateItemList = false

	val essenceRecipeProvider = EssenceRecipeProvider()
	val recipeCache = BetterRepoRecipeCache(essenceRecipeProvider, ReforgeStore)
	val miningData = MiningRepoData()
	val overlayData = ModernOverlaysData()

	fun makeNEURepository(path: Path): NEURepository {
		return NEURepository.of(path).apply {
			registerReloadListener(overlayData)
			registerReloadListener(ItemCache)
			registerReloadListener(RepoItemTypeCache)
			registerReloadListener(ExpLadders)
			registerReloadListener(ItemNameLookup)
			registerReloadListener(ReforgeStore)
			registerReloadListener(essenceRecipeProvider)
			registerReloadListener(recipeCache)
			registerReloadListener(miningData)
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

	fun downloadOverridenBranch(branch: String) {
		Firmament.coroutineScope.launch {
			RepoDownloadManager.downloadUpdate(true, branch)
			reload()
		}
	}

	fun launchAsyncUpdate(force: Boolean = false) {
		Firmament.coroutineScope.launch {
			RepoDownloadManager.downloadUpdate(force)
			reload()
		}
	}

	fun reloadForTest(from: Path) {
		neuRepo = makeNEURepository(from)
		reloadSync()
	}


	suspend fun reload() {
		withContext(Dispatchers.IO) {
			reloadSync()
		}
	}

	fun reloadSync() {
		try {
			logger.info("Repo reload started.")
			neuRepo.reload()
			logger.info("Repo reload completed.")
		} catch (exc: NEURepositoryException) {
			ErrorUtil.softError("Failed to reload repository", exc)
			MC.sendChat(
				tr(
					"firmament.repo.reloadfail",
					"Failed to reload repository. This will result in some mod features not working."
				)
			)
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
			Firmament.coroutineScope.launch {
				reload()
			}
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

	fun shouldLoadREI(): Boolean = Config.enableREI
}
