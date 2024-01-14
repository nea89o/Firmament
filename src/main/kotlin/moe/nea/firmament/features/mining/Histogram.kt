/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.mining

import java.util.*
import kotlin.time.Duration
import moe.nea.firmament.util.TimeMark

class Histogram<T>(
    val maxSize: Int,
    val maxDuration: Duration,
) {

    data class OrderedTimestamp(val timestamp: TimeMark, val order: Int) : Comparable<OrderedTimestamp> {
        override fun compareTo(other: OrderedTimestamp): Int {
            val o = timestamp.compareTo(other.timestamp)
            if (o != 0) return o
            return order.compareTo(other.order)
        }
    }

    val size: Int get() = dataPoints.size
    private val dataPoints: NavigableMap<OrderedTimestamp, T> = TreeMap()

    private var order = Int.MIN_VALUE

    fun record(entry: T, timestamp: TimeMark = TimeMark.now()) {
        dataPoints[OrderedTimestamp(timestamp, order++)] = entry
        trim()
    }

    fun oldestUpdate(): TimeMark {
        trim()
        return if (dataPoints.isEmpty()) TimeMark.now() else dataPoints.firstKey().timestamp
    }

    fun latestUpdate(): TimeMark {
        trim()
        return if (dataPoints.isEmpty()) TimeMark.farPast() else dataPoints.lastKey().timestamp
    }

    fun averagePer(valueExtractor: (T) -> Double, perDuration: Duration): Double? {
        return aggregate(
            seed = 0.0,
            operator = { accumulator, entry, _ -> accumulator + valueExtractor(entry) },
            finish = { sum, beginning, end ->
                val timespan = end - beginning
                if (timespan > perDuration)
                    sum / (timespan / perDuration)
                else null
            })
    }

    fun <V, R> aggregate(
        seed: V,
        operator: (V, T, TimeMark) -> V,
        finish: (V, TimeMark, TimeMark) -> R
    ): R? {
        trim()
        var accumulator = seed
        var min: TimeMark? = null
        var max: TimeMark? = null
        dataPoints.forEach { (key, value) ->
            max = key.timestamp
            if (min == null)
                min = key.timestamp
            accumulator = operator(accumulator, value, key.timestamp)
        }
        if (min == null)
            return null
        return finish(accumulator, min!!, max!!)
    }

    private fun trim() {
        while (maxSize < dataPoints.size) {
            dataPoints.pollFirstEntry()
        }
        dataPoints.headMap(OrderedTimestamp(TimeMark.ago(maxDuration), Int.MAX_VALUE)).clear()
    }


}
