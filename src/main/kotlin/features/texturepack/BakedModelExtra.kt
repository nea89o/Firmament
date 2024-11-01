
package moe.nea.firmament.features.texturepack

import net.minecraft.client.render.model.BakedModel

interface BakedModelExtra {
	var tintOverrides_firmament: TintOverrides?

	fun getHeadModel_firmament(): BakedModel?
    fun setHeadModel_firmament(headModel: BakedModel?)
}
