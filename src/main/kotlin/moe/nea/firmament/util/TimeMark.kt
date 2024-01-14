/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class TimeMark private constructor(private val timeMark: Long) : Comparable<TimeMark> {
    fun passedTime() = if (timeMark == 0L) Duration.INFINITE else (System.currentTimeMillis() - timeMark).milliseconds

    operator fun minus(other: TimeMark): Duration {
        if (other.timeMark == timeMark)
            return 0.milliseconds
        if (other.timeMark == 0L)
            return Duration.INFINITE
        if (timeMark == 0L)
            return -Duration.INFINITE
        return (timeMark - other.timeMark).milliseconds
    }

    companion object {
        fun now() = TimeMark(System.currentTimeMillis())
        fun farPast() = TimeMark(0L)
        fun ago(timeDelta: Duration): TimeMark {
            if (timeDelta.isFinite()) {
                return TimeMark(System.currentTimeMillis() - timeDelta.inWholeMilliseconds)
            }
            require(timeDelta.isPositive())
            return farPast()
        }
    }

    override fun hashCode(): Int {
        return timeMark.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is TimeMark && other.timeMark == timeMark
    }

    override fun compareTo(other: TimeMark): Int {
        return this.timeMark.compareTo(other.timeMark)
    }
}
