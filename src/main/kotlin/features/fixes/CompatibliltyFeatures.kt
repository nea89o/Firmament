package moe.nea.firmament.features.fixes

import net.minecraft.particle.ParticleTypes
import net.minecraft.util.math.Vec3d
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ParticleSpawnEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.compatloader.CompatLoader

object CompatibliltyFeatures : FirmamentFeature {
	override val identifier: String
		get() = "compatibility"

	object TConfig : ManagedConfig(identifier, Category.INTEGRATIONS) {
		val enhancedExplosions by toggle("explosion-enabled") { false }
		val explosionSize by integer("explosion-power", 10, 50) { 1 }
	}

	override val config: ManagedConfig?
		get() = TConfig

	interface ExplosiveApiWrapper {
		fun spawnParticle(vec3d: Vec3d, power: Float)

		companion object : CompatLoader<ExplosiveApiWrapper>(ExplosiveApiWrapper::class.java)
	}

	private val explosiveApiWrapper = ExplosiveApiWrapper.singleInstance

	@Subscribe
	fun onExplosion(it: ParticleSpawnEvent) {
		if (TConfig.enhancedExplosions &&
			it.particleEffect.type == ParticleTypes.EXPLOSION_EMITTER &&
			explosiveApiWrapper != null
		) {
			it.cancel()
			explosiveApiWrapper.spawnParticle(it.position, 2F)
		}
	}
}
