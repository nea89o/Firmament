/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

/**
 * Less aggressive version of `require(obj != null)`, which fails in devenv but continues at runtime.
 */
inline fun <T : Any> assertNotNullOr(obj: T?, message: String? = null, block: () -> T): T {
    if (message == null)
        assert(obj != null)
    else
        assert(obj != null) { message }
    return obj ?: block()
}


/**
 * Less aggressive version of `require(condition)`, which fails in devenv but continues at runtime.
 */
inline fun assertTrueOr(condition: Boolean, block: () -> Unit) {
    assert(condition)
    if (!condition) block()
}


