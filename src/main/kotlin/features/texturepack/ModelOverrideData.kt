package moe.nea.firmament.features.texturepack

import net.minecraft.client.render.model.json.ModelOverride

interface ModelOverrideData {
	companion object {

		@JvmStatic
		@Suppress("CAST_NEVER_SUCCEEDS")
		fun cast(override: ModelOverride) = override as ModelOverrideData
	}

	fun getFirmamentOverrides(): Array<FirmamentModelPredicate>?
	fun setFirmamentOverrides(overrides: Array<FirmamentModelPredicate>?)
}
