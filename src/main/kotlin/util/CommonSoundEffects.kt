

package moe.nea.firmament.util

import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.sounds.SoundEvent
import net.minecraft.resources.Identifier

// TODO: Replace these with custom sound events that just re use the vanilla ogg s
object CommonSoundEffects {
    fun playSound(identifier: Identifier) {
        MC.soundManager.play(SimpleSoundInstance.forUI(SoundEvent.createVariableRangeEvent(identifier), 1F))
    }

    fun playFailure() {
        playSound(Identifier.fromNamespaceAndPath("minecraft", "block.anvil.place"))
    }

    fun playSuccess() {
        playDing()
    }

    fun playDing() {
        playSound(Identifier.fromNamespaceAndPath("minecraft", "entity.arrow.hit_player"))
    }
}
