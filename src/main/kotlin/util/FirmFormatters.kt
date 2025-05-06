package moe.nea.firmament.util

import com.google.common.math.IntMath.pow
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.fileSize
import kotlin.io.path.isDirectory
import kotlin.io.path.isReadable
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

object FirmFormatters {

	private inline fun shortIf(
		value: Double, breakpoint: Double, char: String,
		return_: (String) -> Nothing
	) {
		if (value >= breakpoint) {
			val broken = (value / breakpoint * 10).roundToInt()
			if (broken > 99)
				return_((broken / 10).toString() + char)
			val decimals = broken.toString()
			decimals.singleOrNull()?.let {
				return_("0.$it$char")
			}
			return_("${decimals[0]}.${decimals[1]}$char")
		}
	}

	fun shortFormat(double: Double): String {
		if (double < 0) return "-" + shortFormat(-double)
		shortIf(double, 1_000_000_000_000.0, "t") { return it }
		shortIf(double, 1_000_000_000.0, "b") { return it }
		shortIf(double, 1_000_000.0, "m") { return it }
		shortIf(double, 1_000.0, "k") { return it }
		shortIf(double, 1.0, "") { return it }
		return double.toString()
	}

	fun formatCommas(int: Int, segments: Int = 3): String = formatCommas(int.toLong(), segments)
	fun formatCommas(long: Long, segments: Int = 3, includeSign: Boolean = false): String {
		if (long < 0 && long != Long.MIN_VALUE) {
			return "-" + formatCommas(-long, segments, false)
		}
		val prefix = if (includeSign) "+" else ""
		val α = long / 1000
		if (α != 0L) {
			return prefix + formatCommas(α, segments) + "," + (long - α * 1000).toString().padStart(3, '0')
		}
		return prefix + long.toString()
	}

	fun formatCommas(float: Float, fractionalDigits: Int): String = formatCommas(float.toDouble(), fractionalDigits)
	fun formatCommas(double: Double, fractionalDigits: Int, includeSign: Boolean = false): String {
		val long = double.toLong()
		val δ = (double - long).absoluteValue
		val μ = pow(10, fractionalDigits)
		val digits = (μ * δ).toInt().toString().padStart(fractionalDigits, '0').trimEnd('0')
		return formatCommas(long, includeSign = includeSign) + (if (digits.isEmpty()) "" else ".$digits")
	}

	fun formatDistance(distance: Double): String {
		if (distance < 10)
			return "%.1fm".format(distance)
		return "%dm".format(distance.toInt())
	}

	fun formatTimespan(duration: Duration, millis: Boolean = false): String {
		if (duration.isInfinite()) {
			return if (duration.isPositive()) "∞"
			else "-∞"
		}
		val sb = StringBuilder()
		if (duration.isNegative()) sb.append("-")
		duration.toComponents { days, hours, minutes, seconds, nanoseconds ->
			if (days > 0) {
				sb.append(days).append("d")
			}
			if (hours > 0) {
				sb.append(hours).append("h")
			}
			if (minutes > 0) {
				sb.append(minutes).append("m")
			}
			val milliTime = nanoseconds / 1_000_000
			val deciseconds = milliTime / 100
			if (millis) {
				sb.append(seconds).append("s")
				sb.append(milliTime).append("ms")
			} else if (duration.absoluteValue < 5.seconds && deciseconds != 0) {
				sb.append(seconds).append('.').append(deciseconds.digitToChar()).append("s")
			} else {
				sb.append(seconds).append("s")
			}
			Unit
		}
		return sb.toString()
	}

	fun debugPath(path: Path): Text {
		if (!path.exists()) {
			return tr("firmament.path.missing", "$path (missing)").red()
		}
		if (!path.isReadable()) {
			return tr("firmament.path.unreadable", "$path (unreadable)").red()
		}
		if (path.isRegularFile()) {
			return tr("firmament.path.regular",
			          "$path (exists ${formatFileSize(path.fileSize())})").lime()
		}
		if (path.isDirectory()) {
			return tr("firmament.path.directory", "$path (${path.listDirectoryEntries().size} entries)").darkGreen()
		}
		return tr("firmament.path.unknown", "$path (unknown)").purple()
	}

	fun formatFileSize(fileSizeInBytes: Long): String {
		return "${fileSizeInBytes / 1024} KiB"
	}

	fun formatBool(
		boolean: Boolean,
		trueIsGood: Boolean = true,
	): Text {
		val text = Text.literal(boolean.toString())
		return if (boolean == trueIsGood) text.lime() else text.red()
	}

	fun formatPosition(position: BlockPos): Text {
		return Text.literal("x: ${position.x}, y: ${position.y}, z: ${position.z}")
	}

	fun formatPercent(value: Double, decimals: Int = 1): String {
		return "%.${decimals}f%%".format(value * 100)
	}
}
