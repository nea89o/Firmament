
package moe.nea.firmament.features.texturepack

import net.minecraft.client.render.model.Baker
import net.minecraft.util.Identifier

interface JsonUnbakedModelFirmExtra {
	fun storeExtraBaker_firmament(baker: Baker)

    fun setHeadModel_firmament(identifier: Identifier?)
    fun getHeadModel_firmament(): Identifier?

	fun setTintOverrides_firmament(tintOverrides: TintOverrides?)
	fun getTintOverrides_firmament(): TintOverrides

}
