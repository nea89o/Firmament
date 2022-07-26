package moe.nea.notenoughupdates

import dev.architectury.registry.registries.Registries
import io.github.moulberry.repo.NEURepository
import java.nio.file.Path

object NotEnoughUpdates {
    val REGISTRIES by lazy { Registries.get(MOD_ID) }


    const val MOD_ID = "notenoughupdates"

    val neuRepo = NEURepository.of(Path.of("NotEnoughUpdates-REPO")).also {
        it.reload()
    }


    fun init() {

    }
}
