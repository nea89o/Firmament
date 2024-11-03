package moe.nea.firmament.features.texturepack

import net.fabricmc.fabric.api.renderer.v1.model.WrapperBakedModel as WrapperBakedModelFabric
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.WrapperBakedModel
import moe.nea.firmament.util.ErrorUtil

interface BakedModelExtra {
	companion object {
		@JvmStatic
		fun cast(originalModel: BakedModel): BakedModelExtra? {
			var p = originalModel
			for (i in 0..256) {
				p = when (p) {
					is BakedModelExtra -> return p
					is WrapperBakedModel -> p.wrapped
					is WrapperBakedModelFabric -> WrapperBakedModelFabric.unwrap(p)
					else -> break
				}
			}
			ErrorUtil.softError("Could not find a baked model for $originalModel")
			return null
		}
	}

	var tintOverrides_firmament: TintOverrides?

	fun getHeadModel_firmament(): BakedModel?
	fun setHeadModel_firmament(headModel: BakedModel?)
}
