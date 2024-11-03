@file:OptIn(ExperimentalContracts::class)

package moe.nea.firmament.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import moe.nea.firmament.Firmament

@Suppress("NOTHING_TO_INLINE") // Suppressed since i want the logger to not pick up the ErrorUtil stack-frame
object ErrorUtil {
	var aggressiveErrors = run {
		Thread.currentThread().stackTrace.any { it.className.startsWith("org.junit.") } || Firmament.DEBUG
			|| ErrorUtil::class.java.desiredAssertionStatus()
	}

	inline fun softCheck(message: String, check: Boolean) {
		if (!check) softError(message)
	}

	inline fun lazyCheck(message: String, func: () -> Boolean) {
		contract {
			callsInPlace(func, InvocationKind.AT_MOST_ONCE)
		}
		if (!aggressiveErrors) return
		if (func()) return
		error(message)
	}

	inline fun softError(message: String, exception: Throwable) {
		if (aggressiveErrors) throw IllegalStateException(message, exception)
		else Firmament.logger.error(message, exception)
	}

	inline fun softError(message: String) {
		if (aggressiveErrors) error(message)
		else Firmament.logger.error(message)
	}

	inline fun <T : Any> notNullOr(nullable: T?, message: String, orElse: () -> T): T {
		contract {
			callsInPlace(orElse, InvocationKind.AT_MOST_ONCE)
		}
		if (nullable == null) {
			softError(message)
			return orElse()
		}
		return nullable
	}

}
