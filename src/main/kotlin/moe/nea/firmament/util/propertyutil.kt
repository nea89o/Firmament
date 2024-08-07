

package moe.nea.firmament.util

import kotlin.properties.ReadOnlyProperty

fun <T, V, M> ReadOnlyProperty<T, V>.map(mapper: (V) -> M): ReadOnlyProperty<T, M> {
    return ReadOnlyProperty { thisRef, property -> mapper(this@map.getValue(thisRef, property)) }
}
