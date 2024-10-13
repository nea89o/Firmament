package moe.nea.firmament.util

import moe.nea.firmament.Firmament

object ErrorUtil {
	var aggressiveErrors = run {
		Thread.currentThread().stackTrace.any { it.className.startsWith("org.junit.") } || Firmament.DEBUG
	}

	@Suppress("NOTHING_TO_INLINE") // Suppressed since i want the logger to not pick up the ErrorUtil stack-frame
	inline fun softError(message: String) {
		if (aggressiveErrors) error(message)
		else Firmament.logger.error(message)
	}

}
