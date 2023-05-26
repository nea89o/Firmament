/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
