/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.texturepack

import com.google.gson.JsonElement
import moe.nea.firmament.util.filter.IteratorFilterSet

class ModelOverrideFilterSet(original: java.util.Set<Map.Entry<String, JsonElement>>) :
    IteratorFilterSet<Map.Entry<String, JsonElement>>(original) {
    companion object {
        @JvmStatic
        fun createFilterSet(set: java.util.Set<*>): java.util.Set<*> {
            return ModelOverrideFilterSet(set as java.util.Set<Map.Entry<String, JsonElement>>) as java.util.Set<*>
        }
    }

    override fun shouldKeepElement(element: Map.Entry<String, JsonElement>): Boolean {
        return !element.key.startsWith("firmament:")
    }
}
