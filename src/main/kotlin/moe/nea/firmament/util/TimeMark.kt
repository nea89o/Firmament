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
class TimeMark private constructor(private val timeMark: TimeSource.Monotonic.ValueTimeMark?) : Comparable<TimeMark> {
    fun passedTime() = timeMark?.elapsedNow() ?: Duration.INFINITE

    companion object {
        fun now() = TimeMark(TimeSource.Monotonic.markNow())
        fun farPast() = TimeMark(null)
    }

    override fun compareTo(other: TimeMark): Int {
        if (this.timeMark == other.timeMark) return 0
        if (this.timeMark == null) return -1
        if (other.timeMark == null) return -1
        return this.timeMark.compareTo(other.timeMark)
    }
}
