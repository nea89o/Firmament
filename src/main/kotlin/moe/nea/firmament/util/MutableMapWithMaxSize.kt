/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

fun <K, V> mutableMapWithMaxSize(maxSize: Int): MutableMap<K, V> = object : LinkedHashMap<K, V>() {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>): Boolean {
        return size > maxSize
    }
}

fun <T, R> ((T) -> R).memoizeIdentity(maxCacheSize: Int): (T) -> R {
    val memoized = { it: IdentityCharacteristics<T> ->
        this(it.value)
    }.memoize(maxCacheSize)
    return { memoized(IdentityCharacteristics(it)) }
}

private val SENTINEL_NULL = java.lang.Object()
fun <T, R> ((T) -> R).memoize(maxCacheSize: Int): (T) -> R {
    val map = mutableMapWithMaxSize<T, Any>(maxCacheSize)
    return {
        val value = map.computeIfAbsent(it) { innerValue ->
            this(innerValue) ?: SENTINEL_NULL
        }
        if (value == SENTINEL_NULL) null as R
        else value as R
    }
}
