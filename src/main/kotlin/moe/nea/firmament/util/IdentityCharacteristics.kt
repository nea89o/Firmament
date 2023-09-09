/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

class IdentityCharacteristics<T>(val value: T) {
    override fun equals(other: Any?): Boolean {
        return value === other
    }

    override fun hashCode(): Int {
        return System.identityHashCode(value)
    }
}
