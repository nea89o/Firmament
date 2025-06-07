package moe.nea.firmament.features.garden

import net.minecraft.entity.passive.WolfSoundVariants
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.SoundReceiveEvent
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.SkyBlockIsland

object HideComposterNoises {
	object TConfig : ManagedConfig("composter", Category.GARDEN) {
		val hideComposterNoises by toggle("no-more-noises") { false }
	}

	val composterSoundEvents: List<SoundEvent> = listOf(
		SoundEvents.BLOCK_PISTON_EXTEND,
		SoundEvents.BLOCK_WATER_AMBIENT,
		SoundEvents.ENTITY_CHICKEN_EGG,
		SoundEvents.WOLF_SOUNDS[WolfSoundVariants.Type.CLASSIC]!!.growlSound().value(),
	)

	@Subscribe
	fun onNoise(event: SoundReceiveEvent) {
		if (!TConfig.hideComposterNoises) return
		if (SBData.skyblockLocation == SkyBlockIsland.GARDEN) {
			if (event.sound.value() in composterSoundEvents)
				event.cancel()
		}
	}
}
