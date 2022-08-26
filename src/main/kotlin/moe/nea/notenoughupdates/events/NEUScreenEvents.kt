package moe.nea.notenoughupdates.events

import moe.nea.notenoughupdates.events.NEUScreenEvents.OnScreenOpen
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.MinecraftClient

object NEUScreenEvents {
    fun interface OnScreenOpen {
        /**
         * Called when a new Screen is opened via [MinecraftClient.setScreen]. If [new] is null, this corresponds to closing a [Screen].
         * @return true to prevent this event from happening.
         */
        fun onScreenOpen(old: Screen?, new: Screen?): Boolean
    }

    val SCREEN_OPEN = EventFactory.createArrayBacked(OnScreenOpen::class.java) { arr ->
        OnScreenOpen { old, new ->
            return@OnScreenOpen arr.asSequence().any { it.onScreenOpen(old, new) }
        }
    }

}
