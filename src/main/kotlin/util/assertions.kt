@file:OptIn(ExperimentalContracts::class)

package moe.nea.firmament.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Less aggressive version of `require(obj != null)`, which fails in devenv but continues at runtime.
 */
inline fun <T : Any> assertNotNullOr(obj: T?, message: String? = null, block: () -> T): T {
	contract {
		callsInPlace(block, InvocationKind.AT_MOST_ONCE)
	}
    if (message == null)
        assert(obj != null)
    else
        assert(obj != null) { message }
    return obj ?: block()
}


/**
 * Less aggressive version of `require(condition)`, which fails in devenv but continues at runtime.
 */
inline fun assertTrueOr(condition: Boolean, block: () -> Unit) {
	contract {
		callsInPlace(block, InvocationKind.AT_MOST_ONCE)
	}
    assert(condition)
    if (!condition) block()
}


