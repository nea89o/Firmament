
package moe.nea.firmament.features.texturepack

import net.minecraft.client.render.model.BakedModel

interface BakedModelExtra {
    fun getHeadModel_firmament(): BakedModel?
    fun setHeadModel_firmament(headModel: BakedModel?)
}
