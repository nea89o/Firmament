
package moe.nea.firmament.features.texturepack

import com.google.gson.JsonElement

interface FirmamentModelPredicateParser {
    fun parse(jsonElement: JsonElement): FirmamentModelPredicate?
}
