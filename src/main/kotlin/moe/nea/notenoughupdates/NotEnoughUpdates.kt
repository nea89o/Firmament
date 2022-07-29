package moe.nea.notenoughupdates

import io.github.moulberry.repo.NEURepository
import net.fabricmc.api.ModInitializer
import java.nio.file.Path

object NotEnoughUpdates : ModInitializer {
    val DATA_DIR = Path.of(".notenoughupdates")

    const val MOD_ID = "notenoughupdates"

    val neuRepo = NEURepository.of(Path.of("NotEnoughUpdates-REPO")).also {
        it.reload()
    }

    override fun onInitialize() {
    }
}
