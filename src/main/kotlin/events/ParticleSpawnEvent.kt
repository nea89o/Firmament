

package moe.nea.firmament.events

import org.joml.Vector3f
import net.minecraft.particle.ParticleEffect
import net.minecraft.util.math.Vec3d

data class ParticleSpawnEvent(
    val particleEffect: ParticleEffect,
    val position: Vec3d,
    val offset: Vector3f,
    val longDistance: Boolean,
    val count: Int,
    val speed: Float,
) : FirmamentEvent.Cancellable() {
    companion object : FirmamentEventBus<ParticleSpawnEvent>()
}
