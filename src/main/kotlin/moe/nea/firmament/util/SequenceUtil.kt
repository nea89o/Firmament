/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

fun <T : Any> T.iterate(iterator: (T) -> T?): Sequence<T> = sequence {
    var x: T? = this@iterate
    while (x != null) {
        yield(x)
        x = iterator(x)
    }
}
