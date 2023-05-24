package moe.nea.firmament.repo

import io.github.cottonmc.cotton.gui.client.CottonHud
import io.github.moulberry.repo.NEURecipeCache
import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.NEURepositoryException
import io.github.moulberry.repo.data.NEUItem
import io.github.moulberry.repo.data.NEURecipe
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import kotlinx.coroutines.launch
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.Firmament.logger
import moe.nea.firmament.hud.ProgressBar
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.gui.config.ManagedConfig

object RepoManager {
    object Config : ManagedConfig("repo") {
        var username by string("username") { "NotEnoughUpdates" }
        var reponame by string("reponame") { "NotEnoughUpdates-REPO" }
        var branch by string("branch") { "prerelease" }
        val autoUpdate by toggle("autoUpdate") { true }
        val reset by button("reset") {
            username = "NotEnoughUpdates"
            reponame = "NotEnoughUpdates-REPO"
            branch = "prerelease"
            save()
        }
    }

    val currentDownloadedSha by RepoDownloadManager::latestSavedVersionHash

    var recentlyFailedToUpdateItemList = false

    val progressBar = ProgressBar("", null, 0).also {
        it.setSize(180, 22)
    }

    val neuRepo: NEURepository = NEURepository.of(RepoDownloadManager.repoSavedLocation).apply {
        registerReloadListener(ItemCache)
        registerReloadListener {
            if (!trySendClientboundUpdateRecipesPacket()) {
                logger.warn("Failed to issue a ClientboundUpdateRecipesPacket (to reload REI). This may lead to an outdated item list.")
                recentlyFailedToUpdateItemList = true
            }
        }
    }

    private val recipeCache = NEURecipeCache.forRepo(neuRepo)

    fun getAllRecipes() = neuRepo.items.items.values.asSequence().flatMap { it.recipes }

    fun getRecipesFor(skyblockId: SkyblockId): Set<NEURecipe> = recipeCache.recipes[skyblockId.neuItem] ?: setOf()
    fun getUsagesFor(skyblockId: SkyblockId): Set<NEURecipe> = recipeCache.usages[skyblockId.neuItem] ?: setOf()

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
            progressBar.reportProgress("Downloading", 0, null)
            CottonHud.add(progressBar)
            RepoDownloadManager.downloadUpdate(force)
            progressBar.reportProgress("Download complete", 1, 1)
            reload()
        }
    }

    fun reload() {
        try {
            progressBar.reportProgress("Reloading from Disk", 0, null)
            CottonHud.add(progressBar)
            neuRepo.reload()
        } catch (exc: NEURepositoryException) {
            MinecraftClient.getInstance().player?.sendMessage(
                Text.literal("Failed to reload repository. This will result in some mod features not working.")
            )
            CottonHud.remove(progressBar)
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

}
