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
    fun toString(float: Float, fractionalDigits: Int): String = toString(float.toDouble(), fractionalDigits)
    fun toString(double: Double, fractionalDigits: Int): String {
        val long = double.toLong()
        val δ = (double - long).absoluteValue
        val μ = pow(10, fractionalDigits)
        val digits = (μ * δ).toInt().toString().padStart(fractionalDigits, '0').trimEnd('0')
        return long.toString() + (if (digits.isEmpty()) "" else ".$digits")
    }

    fun formatTimespan(duration: Duration): String {
        return duration.toString()
    }

}
