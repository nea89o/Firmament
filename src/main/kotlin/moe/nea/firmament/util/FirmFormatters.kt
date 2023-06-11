package moe.nea.firmament.util

import com.google.common.math.IntMath.pow
import kotlin.math.absoluteValue

object FirmFormatters {
    fun toString(float: Float, fractionalDigits: Int): String = toString(float.toDouble(), fractionalDigits)
    fun toString(double: Double, fractionalDigits: Int): String {
        val long = double.toLong()
        val δ = (double - long).absoluteValue
        val μ = pow(10, fractionalDigits)
        val digits = (μ * δ).toInt().toString().padStart(fractionalDigits, '0').trimEnd('0')
        return long.toString() + (if (digits.isEmpty()) "" else ".$digits")
    }

}
