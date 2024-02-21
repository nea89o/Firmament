/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util.filter

abstract class IteratorFilterSet<K>(val original: java.util.Set<K>) : java.util.Set<K> by original {
    abstract fun shouldKeepElement(element: K): Boolean

    override fun iterator(): MutableIterator<K> {
        val parentIterator = original.iterator()
        return object : MutableIterator<K> {
            var lastEntry: K? = null
            override fun hasNext(): Boolean {
                while (lastEntry == null) {
                    if (!parentIterator.hasNext())
                        break
                    val element = parentIterator.next()
                    if (!shouldKeepElement(element)) continue
                    lastEntry = element
                }
                return lastEntry != null
            }

            override fun next(): K {
                if (!hasNext()) throw NoSuchElementException()
                return lastEntry ?: throw NoSuchElementException()
            }

            override fun remove() {
                TODO("Not yet implemented")
            }
        }
    }
}

