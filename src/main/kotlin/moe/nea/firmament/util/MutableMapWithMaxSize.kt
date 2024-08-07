
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

@PublishedApi
internal val SENTINEL_NULL = java.lang.Object()

/**
 * Requires the map to only contain values of type [R] or [SENTINEL_NULL]. This is ensured if the map is only ever
 * accessed via this function.
 */
inline fun <T, R> MutableMap<T, Any>.computeNullableFunction(key: T, crossinline func: () -> R): R {
    val value = this.getOrPut(key) {
        func() ?: SENTINEL_NULL
    }
    @Suppress("UNCHECKED_CAST")
    return if (value === SENTINEL_NULL) null as R
    else value as R
}

fun <T, R> ((T) -> R).memoize(maxCacheSize: Int): (T) -> R {
    val map = mutableMapWithMaxSize<T, Any>(maxCacheSize)
    return {
        map.computeNullableFunction(it) { this@memoize(it) }
    }
}
