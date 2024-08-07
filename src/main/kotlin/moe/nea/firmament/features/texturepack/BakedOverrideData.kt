
package moe.nea.firmament.features.texturepack

interface BakedOverrideData {
    fun getFirmamentOverrides(): Array<FirmamentModelPredicate>?
    fun setFirmamentOverrides(overrides: Array<FirmamentModelPredicate>?)

}
