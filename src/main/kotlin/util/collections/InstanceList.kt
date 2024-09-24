package moe.nea.firmament.util.collections

import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference

class InstanceList<T : Any>(val name: String) {
    val queue = object : ReferenceQueue<T>() {}
    val set = mutableSetOf<Ref>()

    val size: Int
        get() {
            clearOldReferences()
            return set.size
        }

    fun clearOldReferences() {
        while (true) {
            val reference = queue.poll() ?: break
            set.remove(reference)
        }
    }

    fun getAll(): List<T> {
        clearOldReferences()
        return set.mapNotNull { it.get() }
    }

    fun add(t: T) {
        set.add(Ref(t))
    }

    init {
        if (init)
            allInstances.add(this)
    }

    inner class Ref(referent: T) : WeakReference<T>(referent) {
        val hashCode = System.identityHashCode(referent)
        override fun equals(other: Any?): Boolean {
            return other is InstanceList<*>.Ref && hashCode == other.hashCode && get() === other.get()
        }

        override fun hashCode(): Int {
            return hashCode
        }
    }

    companion object {
        private var init = false
        val allInstances = InstanceList<InstanceList<*>>("InstanceLists")

        init {
            init = true
            allInstances.add(allInstances)
        }
    }
}
