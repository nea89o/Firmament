package moe.nea.firmament.util.collections

import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import moe.nea.firmament.features.debug.DebugLogger

/**
 * Cache class that uses [WeakReferences][WeakReference] to only cache values while there is still a life reference to
 * the key. Each key can have additional extra data that is used to look up values. That extra data is not required to
 * be a life reference. The main Key is compared using strict reference equality. This map is not synchronized.
 */
class WeakCache<Key : Any, ExtraKey : Any, Value : Any>(val name: String) {
    private val queue = object : ReferenceQueue<Key>() {}
    private val map = mutableMapOf<Ref, Value>()

    val size: Int
        get() {
            clearOldReferences()
            return map.size
        }

    fun clearOldReferences() {
        var successCount = 0
        var totalCount = 0
        while (true) {
            val reference = queue.poll() ?: break
            totalCount++
            if (map.remove(reference) != null)
                successCount++
        }
        if (totalCount > 0)
            logger.log { "Cleared $successCount/$totalCount references from queue" }
    }

    fun get(key: Key, extraData: ExtraKey): Value? {
        clearOldReferences()
        return map[Ref(key, extraData)]
    }

    fun put(key: Key, extraData: ExtraKey, value: Value) {
        clearOldReferences()
        map[Ref(key, extraData)] = value
    }

    fun getOrPut(key: Key, extraData: ExtraKey, value: (Key, ExtraKey) -> Value): Value {
        clearOldReferences()
        return map.getOrPut(Ref(key, extraData)) { value(key, extraData) }
    }

    fun clear() {
        map.clear()
    }

    init {
        allInstances.add(this)
    }

    companion object {
        val allInstances = InstanceList<WeakCache<*, *, *>>("WeakCaches")
        private val logger = DebugLogger("WeakCache")
        fun <Key : Any, Value : Any> memoize(name: String, function: (Key) -> Value):
            CacheFunction.NoExtraData<Key, Value> {
            return CacheFunction.NoExtraData(WeakCache(name), function)
        }

        fun <Key : Any, ExtraKey : Any, Value : Any> memoize(name: String, function: (Key, ExtraKey) -> Value):
            CacheFunction.WithExtraData<Key, ExtraKey, Value> {
            return CacheFunction.WithExtraData(WeakCache(name), function)
        }
    }

    inner class Ref(
        weakInstance: Key,
        val extraData: ExtraKey,
    ) : WeakReference<Key>(weakInstance, queue) {
        val hashCode = System.identityHashCode(weakInstance) * 31 + extraData.hashCode()
        override fun equals(other: Any?): Boolean {
            if (other !is WeakCache<*, *, *>.Ref) return false
            return other.hashCode == this.hashCode
                && other.get() === this.get()
                && other.extraData == this.extraData
        }

        override fun hashCode(): Int {
            return hashCode
        }
    }

    interface CacheFunction {
        val cache: WeakCache<*, *, *>

        data class NoExtraData<Key : Any, Value : Any>(
            override val cache: WeakCache<Key, Unit, Value>,
            val wrapped: (Key) -> Value,
        ) : CacheFunction, (Key) -> Value {
            override fun invoke(p1: Key): Value {
                return cache.getOrPut(p1, Unit, { a, _ -> wrapped(a) })
            }
        }

        data class WithExtraData<Key : Any, ExtraKey : Any, Value : Any>(
            override val cache: WeakCache<Key, ExtraKey, Value>,
            val wrapped: (Key, ExtraKey) -> Value,
        ) : CacheFunction, (Key, ExtraKey) -> Value {
            override fun invoke(p1: Key, p2: ExtraKey): Value {
                return cache.getOrPut(p1, p2, wrapped)
            }
        }
    }
}
