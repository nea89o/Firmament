
package moe.nea.firmament.features.texturepack

interface ModelOverrideData {
    fun getFirmamentOverrides(): Array<FirmamentModelPredicate>?
    fun setFirmamentOverrides(overrides: Array<FirmamentModelPredicate>?)
}
