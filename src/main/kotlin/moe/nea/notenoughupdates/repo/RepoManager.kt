package moe.nea.notenoughupdates.repo

import io.github.moulberry.repo.NEURepository
import kotlinx.coroutines.launch
import moe.nea.notenoughupdates.NotEnoughUpdates
import moe.nea.notenoughupdates.NotEnoughUpdates.logger
import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket

object RepoManager {


    val neuRepo: NEURepository = NEURepository.of(RepoDownloadManager.repoSavedLocation).apply {
        registerReloadListener(ItemCache)
        registerReloadListener {
            if (Minecraft.getInstance().connection?.handleUpdateRecipes(ClientboundUpdateRecipesPacket(mutableListOf())) == null) {
                logger.warn("Failed to issue a ClientboundUpdateRecipesPacket (to reload REI). This may lead to an outdated item list.")
            }
        }
    }


    fun launchAsyncUpdate() {
        NotEnoughUpdates.coroutineScope.launch {
            if (RepoDownloadManager.downloadUpdate()) {
                neuRepo.reload()
            }
        }
    }

}
