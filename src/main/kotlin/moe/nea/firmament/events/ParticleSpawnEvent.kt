package moe.nea.firmament.events

import net.minecraft.particle.ParticleEffect
import net.minecraft.util.math.Vec3d

data class ParticleSpawnEvent(
    val particleEffect: ParticleEffect,
    val position: Vec3d,
    val offset: Vec3d,
    val longDistance: Boolean,
) : FirmamentEvent() {
    companion object : FirmamentEventBus<ParticleSpawnEvent>()
}
