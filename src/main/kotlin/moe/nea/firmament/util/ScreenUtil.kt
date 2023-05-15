package moe.nea.firmament.util

import moe.nea.firmament.Firmament
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen

object ScreenUtil {
    init {
        ClientTickEvents.START_CLIENT_TICK.register(::onTick)
    }

    private fun onTick(minecraft: MinecraftClient) {
        if (nextOpenedGui != null) {
            val p = minecraft.player
            if (p?.currentScreenHandler != null) {
                p.closeHandledScreen()
            }
            minecraft.setScreen(nextOpenedGui)
            nextOpenedGui = null
        }
    }

    private var nextOpenedGui: Screen? = null

    fun setScreenLater(nextScreen: Screen) {
        val nog = nextOpenedGui
        if (nog != null) {
            Firmament.logger.warn("Setting screen ${nextScreen::class.qualifiedName} to be opened later, but ${nog::class.qualifiedName} is already queued.")
            return
        }
        nextOpenedGui = nextScreen
    }


}
