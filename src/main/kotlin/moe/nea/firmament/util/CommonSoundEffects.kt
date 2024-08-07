

package moe.nea.firmament.util

import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Identifier

// TODO: Replace these with custom sound events that just re use the vanilla ogg s
object CommonSoundEffects {
    fun playSound(identifier: Identifier) {
        MC.soundManager.play(PositionedSoundInstance.master(SoundEvent.of(identifier), 1F))
    }

    fun playFailure() {
        playSound(Identifier.of("minecraft", "block.anvil.place"))
    }

    fun playSuccess() {
        playDing()
    }

    fun playDing() {
        playSound(Identifier.of("minecraft", "entity.arrow.hit_player"))
    }
}
