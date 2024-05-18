/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

class IdentityCharacteristics<T>(val value: T) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IdentityCharacteristics<*>) return false
        return value === other.value
    }

    override fun hashCode(): Int {
        return System.identityHashCode(value)
    }
}
