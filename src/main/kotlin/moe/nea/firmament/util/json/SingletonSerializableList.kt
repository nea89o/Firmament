/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement

class SingletonSerializableList<T>(val child: KSerializer<T>) : KSerializer<List<T>> {
    override val descriptor: SerialDescriptor
        get() = JsonElement.serializer().descriptor

    override fun deserialize(decoder: Decoder): List<T> {
        decoder as JsonDecoder
        val list = JsonElement.serializer().deserialize(decoder)
        if (list is JsonArray) {
            return list.map {
                decoder.json.decodeFromJsonElement(child, it)
            }
        }
        return listOf(decoder.json.decodeFromJsonElement(child, list))
    }

    override fun serialize(encoder: Encoder, value: List<T>) {
        ListSerializer(child).serialize(encoder, value)
    }
}
