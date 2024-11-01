package moe.nea.firmament.util

import moe.nea.firmament.Firmament

object ErrorUtil {
	var aggressiveErrors = run {
		Thread.currentThread().stackTrace.any { it.className.startsWith("org.junit.") } || Firmament.DEBUG
	}

	inline fun softCheck(message: String, func: () -> Boolean) {
		if (!aggressiveErrors) return
		if (func()) return
		error(message)
	}

	@Suppress("NOTHING_TO_INLINE") // Suppressed since i want the logger to not pick up the ErrorUtil stack-frame
	inline fun softError(message: String) {
		if (aggressiveErrors) error(message)
		else Firmament.logger.error(message)
	}

	inline fun <T : Any> notNullOr(nullable: T?, message: String, orElse: () -> T): T {
		if (nullable == null) {
			softError(message)
			return orElse()
		}
		return nullable
	}

}
