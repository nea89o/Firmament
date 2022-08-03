package moe.nea.notenoughupdates.repo

import io.github.moulberry.repo.NEURepository
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import moe.nea.notenoughupdates.NotEnoughUpdates
import moe.nea.notenoughupdates.NotEnoughUpdates.logger
import moe.nea.notenoughupdates.util.ConfigHolder
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
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
        return Minecraft.getInstance().level != null && Minecraft.getInstance().connection?.handleUpdateRecipes(ClientboundUpdateRecipesPacket(mutableListOf())) != null
    }

    init {
        ClientTickEvents.START_WORLD_TICK.register(ClientTickEvents.StartWorldTick {
            if (recentlyFailedToUpdateItemList && trySendClientboundUpdateRecipesPacket())
                recentlyFailedToUpdateItemList = false
        })
    }

    fun launchAsyncUpdate() {
        NotEnoughUpdates.coroutineScope.launch {
            RepoDownloadManager.downloadUpdate()
            neuRepo.reload()
        }
    }

    fun initialize() {
        if (config.autoUpdate) {
            launchAsyncUpdate()
        } else {
            neuRepo.reload()
        }
    }

}
