

package moe.nea.firmament.features.fixes

import net.fabricmc.loader.api.FabricLoader
import net.superkat.explosiveenhancement.api.ExplosiveApi
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.math.Vec3d
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ParticleSpawnEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC

object CompatibliltyFeatures : FirmamentFeature {
    override val identifier: String
        get() = "compatibility"

    object TConfig : ManagedConfig(identifier) {
        val enhancedExplosions by toggle("explosion-enabled") { false }
        val explosionSize by integer("explosion-power", 10, 50) { 1 }
    }

    override val config: ManagedConfig?
        get() = TConfig

    interface ExplosiveApiWrapper {
        fun spawnParticle(vec3d: Vec3d, power: Float)
    }

    class ExplosiveApiWrapperImpl : ExplosiveApiWrapper {
        override fun spawnParticle(vec3d: Vec3d, power: Float) {
            ExplosiveApi.spawnParticles(MC.world, vec3d.x, vec3d.y, vec3d.z, TConfig.explosionSize / 10F)
        }
    }

    val explosiveApiWrapper = if (FabricLoader.getInstance().isModLoaded("explosiveenhancement")) {
        ExplosiveApiWrapperImpl()
    } else null

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
