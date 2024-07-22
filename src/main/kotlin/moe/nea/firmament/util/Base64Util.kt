/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

object Base64Util {
    fun String.padToValidBase64(): String {
        val align = this.length % 4
        if (align == 0) return this
        return this + "=".repeat(4 - align)
    }
}
