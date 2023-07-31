/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import kotlin.properties.ReadOnlyProperty

fun <T, V, M> ReadOnlyProperty<T, V>.map(mapper: (V) -> M): ReadOnlyProperty<T, M> {
    return ReadOnlyProperty { thisRef, property -> mapper(this@map.getValue(thisRef, property)) }
}
