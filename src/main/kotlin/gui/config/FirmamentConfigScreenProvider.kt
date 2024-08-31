package moe.nea.firmament.gui.config

import java.util.ServiceLoader
import kotlin.streams.asSequence
import net.minecraft.client.gui.screen.Screen
import moe.nea.firmament.Firmament

interface FirmamentConfigScreenProvider {
    val key: String
    val isEnabled: Boolean get() = true

    fun open(parent: Screen?): Screen

    companion object {
        private val loader = ServiceLoader.load(FirmamentConfigScreenProvider::class.java)

        val providers by lazy {
            loader.stream().asSequence().mapNotNull { service ->
                kotlin.runCatching { service.get() }
                    .getOrElse {
                        Firmament.logger.warn("Could not load config provider ${service.type()}", it)
                        null
                    }
            }.filter { it.isEnabled }.toList()
        }
    }
}
