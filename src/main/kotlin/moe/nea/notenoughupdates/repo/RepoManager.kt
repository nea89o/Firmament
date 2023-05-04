package moe.nea.notenoughupdates.repo

import io.github.cottonmc.cotton.gui.client.CottonHud
import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.NEURepositoryException
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import moe.nea.notenoughupdates.NotEnoughUpdates
import moe.nea.notenoughupdates.NotEnoughUpdates.logger
import moe.nea.notenoughupdates.hud.ProgressBar
import moe.nea.notenoughupdates.util.data.DataHolder
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket
import net.minecraft.text.Text
import moe.nea.notenoughupdates.util.SkyblockId

object RepoManager : DataHolder<RepoManager.Config>(serializer(), "repo", ::Config) {
    @Serializable
    data class Config(
        var user: String = "NotEnoughUpdates",
        var repo: String = "NotEnoughUpdates-REPO",
        var autoUpdate: Boolean = true,
        var branch: String = "dangerous",
    )

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

    fun getNEUItem(skyblockId: SkyblockId) = neuRepo.items.getItemBySkyblockId(skyblockId.neuItem)

    fun launchAsyncUpdate(force: Boolean = false) {
        NotEnoughUpdates.coroutineScope.launch {
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
        if (data.autoUpdate) {
            launchAsyncUpdate()
        } else {
            reload()
        }
    }

}
