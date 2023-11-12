/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import com.google.common.math.IntMath.pow
import kotlin.math.absoluteValue
import kotlin.time.Duration

object FirmFormatters {
    fun formatCurrency(long: Long, segments: Int = 3): String {
        val α = long / 1000
        if (α != 0L) {
            return formatCurrency(α, segments) + "," + (long - α * 1000).toString().padStart(3, '0')
        }
        return long.toString()
    }

    fun formatCurrency(float: Float, fractionalDigits: Int): String = formatCurrency(float.toDouble(), fractionalDigits)
    fun formatCurrency(double: Double, fractionalDigits: Int): String {
        val long = double.toLong()
        val δ = (double - long).absoluteValue
        val μ = pow(10, fractionalDigits)
        val digits = (μ * δ).toInt().toString().padStart(fractionalDigits, '0').trimEnd('0')
        return formatCurrency(long) + (if (digits.isEmpty()) "" else ".$digits")
    }

    fun formatDistance(distance: Double): String {
        if (distance < 10)
            return "%.1fm".format(distance)
        return "%dm".format(distance.toInt())
    }

    fun formatTimespan(duration: Duration): String {
        return duration.toString()
    }

}
