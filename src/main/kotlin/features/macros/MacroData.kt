package moe.nea.firmament.features.macros

import kotlinx.serialization.Serializable
import moe.nea.firmament.util.data.DataHolder

@Serializable
data class MacroData(
    var comboActions: List<ComboKeyAction> = listOf(),
){
	object DConfig : DataHolder<MacroData>(kotlinx.serialization.serializer(), "macros", ::MacroData)
}
