/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

/**
 * Less aggressive version of `require(obj != null)`, which fails in devenv but continues at runtime.
 */
inline fun <T : Any> assertNotNullOr(obj: T?, block: () -> T): T {
    assert(obj != null)
    return obj ?: block()
}


/**
 * Less aggressive version of `require(condition)`, which fails in devenv but continues at runtime.
 */
inline fun assertTrueOr(condition: Boolean, block: () -> Unit) {
    assert(condition)
    if (!condition) block()
}


