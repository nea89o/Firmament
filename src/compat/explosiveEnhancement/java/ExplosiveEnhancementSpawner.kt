package moe.nea.firmament.compat.explosiveenhancement

import com.google.auto.service.AutoService
import net.superkat.explosiveenhancement.api.ExplosiveApi
import net.minecraft.util.math.Vec3d
import moe.nea.firmament.features.fixes.CompatibliltyFeatures
import moe.nea.firmament.features.fixes.CompatibliltyFeatures.TConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.compatloader.CompatLoader

@AutoService(CompatibliltyFeatures.ExplosiveApiWrapper::class)
@CompatLoader.RequireMod("explosiveenhancement")
class ExplosiveEnhancementSpawner : CompatibliltyFeatures.ExplosiveApiWrapper {
	override fun spawnParticle(vec3d: Vec3d, power: Float) {
		ExplosiveApi.spawnParticles(MC.world, vec3d.x, vec3d.y, vec3d.z, TConfig.explosionSize / 10F)
	}
}
