
package moe.nea.firmament.features.texturepack

import net.minecraft.client.render.model.json.ModelOverrideList

interface BakedOverrideData {
    fun getFirmamentOverrides(): Array<FirmamentModelPredicate>?
    fun setFirmamentOverrides(overrides: Array<FirmamentModelPredicate>?)
	companion object{
		@Suppress("CAST_NEVER_SUCCEEDS")
		@JvmStatic
		fun cast(bakedOverride: ModelOverrideList.BakedOverride): BakedOverrideData = bakedOverride as BakedOverrideData
	}
}
