@file:OptIn(ExperimentalContracts::class)

package moe.nea.firmament.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import moe.nea.firmament.Firmament

@Suppress("NOTHING_TO_INLINE") // Suppressed since i want the logger to not pick up the ErrorUtil stack-frame
object ErrorUtil {
	var aggressiveErrors = run {
		TestUtil.isInTest || Firmament.DEBUG
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

	class Catch<T> private constructor(val value: T?, val exc: Throwable?) {
		fun orNull(): T? = value

		inline fun or(block: (exc: Throwable) -> T): T {
			contract {
				callsInPlace(block, InvocationKind.AT_MOST_ONCE)
			}
			if (exc != null) return block(exc)
			@Suppress("UNCHECKED_CAST")
			return value as T
		}

		companion object {
			fun <T> fail(exception: Throwable): Catch<T> = Catch(null, exception)
			fun <T> succeed(value: T): Catch<T> = Catch(value, null)
		}
	}

	inline fun <T> catch(message: String, block: () -> T): Catch<T> {
		try {
			return Catch.succeed(block())
		} catch (exc: Throwable) {
			softError(message, exc)
			return Catch.fail(exc)
		}
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

	fun softUserError(string: String) {
		MC.sendChat(tr("frimanet.usererror", "Firmament encountered a user caused error: $string"))
	}
}
