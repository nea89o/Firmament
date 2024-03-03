/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.texturepack

import com.google.gson.JsonObject
import net.minecraft.util.Identifier

object CustomModelOverrideParser {

    val predicateParsers = mutableMapOf<Identifier, FirmamentModelPredicateParser>()


    fun registerPredicateParser(name: String, parser: FirmamentModelPredicateParser) {
        predicateParsers[Identifier("firmament", name)] = parser
    }

    init {
        registerPredicateParser("display_name", DisplayNamePredicate.Parser)
        registerPredicateParser("lore", LorePredicate.Parser)
        registerPredicateParser("all", AndPredicate.Parser)
        registerPredicateParser("any", OrPredicate.Parser)
        registerPredicateParser("not", NotPredicate.Parser)
    }

    fun parsePredicates(predicates: JsonObject): List<FirmamentModelPredicate> {
        val parsedPredicates = mutableListOf<FirmamentModelPredicate>()
        for (predicateName in predicates.keySet()) {
            if (!predicateName.startsWith("firmament:")) continue
            val identifier = Identifier(predicateName)
            val parser = predicateParsers[identifier] ?: continue
            val parsedPredicate = parser.parse(predicates[predicateName])
            parsedPredicates.add(parsedPredicate)
        }
        return parsedPredicates
    }

    @JvmStatic
    fun parseCustomModelOverrides(jsonObject: JsonObject): Array<FirmamentModelPredicate>? {
        val predicates = (jsonObject["predicate"] as? JsonObject) ?: return null
        val parsedPredicates = parsePredicates(predicates)
        if (parsedPredicates.isEmpty())
            return null
        return parsedPredicates.toTypedArray()
    }
}
