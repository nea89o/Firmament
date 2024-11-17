package moe.nea.firmament.util.skyblock

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


class ItemType(val name: String) {
	companion object {
		private val generated = object : ReadOnlyProperty<Any?, ItemType> {
			override fun getValue(thisRef: Any?, property: KProperty<*>): ItemType {
				return ItemType.ofName(property.name)
			}
		}

		fun ofName(name: String): ItemType {
			return ItemType(name)
		}

		val SWORD by generated
	}
}
