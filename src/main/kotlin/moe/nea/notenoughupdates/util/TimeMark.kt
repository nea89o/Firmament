package moe.nea.notenoughupdates.util

import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
class TimeMark private constructor(private val timeMark: TimeSource.Monotonic.ValueTimeMark?) {
    fun passedTime() = timeMark?.elapsedNow() ?: Duration.INFINITE

    companion object {
        fun now() = TimeMark(TimeSource.Monotonic.markNow())
        fun farPast() = TimeMark(null)
    }
}
