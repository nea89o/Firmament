/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
class Timer {
    private var mark: TimeSource.Monotonic.ValueTimeMark? = null

    fun timePassed(): Duration {
        return mark?.elapsedNow() ?: Duration.INFINITE
    }

    fun markNow() {
        mark = TimeSource.Monotonic.markNow()
    }

    fun markFarPast() {
        mark = null
    }

}
