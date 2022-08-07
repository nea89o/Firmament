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
import moe.nea.notenoughupdates.util.ConfigHolder
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket

object RepoManager : ConfigHolder<RepoManager.Config>(serializer(), "repo", ::Config) {
    @Serializable
    data class Config(
        var user: String = "NotEnoughUpdates",
        var repo: String = "NotEnoughUpdates-REPO",
        var autoUpdate: Boolean = true,
        var branch: String = "dangerous",
    )

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
        return Minecraft.getInstance().level != null && Minecraft.getInstance().connection?.handleUpdateRecipes(
            ClientboundUpdateRecipesPacket(mutableListOf())
        ) != null
    }

    init {
        ClientTickEvents.START_WORLD_TICK.register(ClientTickEvents.StartWorldTick {
            if (recentlyFailedToUpdateItemList && trySendClientboundUpdateRecipesPacket())
                recentlyFailedToUpdateItemList = false
        })
    }

    fun launchAsyncUpdate() {
        NotEnoughUpdates.coroutineScope.launch {
            progressBar.reportProgress("Downloading", 0, null)
            CottonHud.add(progressBar)
            RepoDownloadManager.downloadUpdate()
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
            Minecraft.getInstance().player?.sendSystemMessage(
                Component.literal("Failed to reload repository. This will result in some mod features not working.")
            )
            exc.printStackTrace()
        }
    }

    fun initialize() {
        if (config.autoUpdate) {
            launchAsyncUpdate()
        } else {
            reload()
        }
    }

}
