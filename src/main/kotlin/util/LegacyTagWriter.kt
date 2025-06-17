package moe.nea.firmament.util

import kotlinx.serialization.json.JsonPrimitive
import net.minecraft.nbt.AbstractNbtList
import net.minecraft.nbt.NbtByte
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtDouble
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtEnd
import net.minecraft.nbt.NbtFloat
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtLong
import net.minecraft.nbt.NbtShort
import net.minecraft.nbt.NbtString
import moe.nea.firmament.util.mc.SNbtFormatter.Companion.SIMPLE_NAME

class LegacyTagWriter(val compact: Boolean) {
	companion object {
		fun stringify(nbt: NbtElement, compact: Boolean): String {
			return LegacyTagWriter(compact).also { it.writeElement(nbt) }
				.stringWriter.toString()
		}

		fun NbtElement.toLegacyString(pretty: Boolean = false): String {
			return stringify(this, !pretty)
		}
	}

	val stringWriter = StringBuilder()
	var indent = 0
	fun newLine() {
		if (compact) return
		stringWriter.append('\n')
		repeat(indent) {
			stringWriter.append("  ")
		}
	}

	fun writeElement(nbt: NbtElement) {
		when (nbt) {
			is NbtInt -> stringWriter.append(nbt.value.toString())
			is NbtString -> stringWriter.append(escapeString(nbt.value))
			is NbtFloat -> stringWriter.append(nbt.value).append('F')
			is NbtDouble -> stringWriter.append(nbt.value).append('D')
			is NbtByte -> stringWriter.append(nbt.value).append('B')
			is NbtLong -> stringWriter.append(nbt.value).append('L')
			is NbtShort -> stringWriter.append(nbt.value).append('S')
			is NbtCompound -> writeCompound(nbt)
			is NbtEnd -> {}
			is AbstractNbtList -> writeArray(nbt)
		}
	}

	fun writeArray(nbt: AbstractNbtList) {
		stringWriter.append('[')
		indent++
		newLine()
		nbt.forEachIndexed { index, element ->
			writeName(index.toString())
			writeElement(element)
			if (index != nbt.size() - 1) {
				stringWriter.append(',')
				newLine()
			}
		}
		indent--
		if (nbt.size() != 0)
			newLine()
		stringWriter.append(']')
	}

	fun writeCompound(nbt: NbtCompound) {
		stringWriter.append('{')
		indent++
		newLine()
		val entries = nbt.entrySet().sortedBy { it.key }
		entries.forEachIndexed { index, it ->
			writeName(it.key)
			writeElement(it.value)
			if (index != entries.lastIndex) {
				stringWriter.append(',')
				newLine()
			}
		}
		indent--
		if (nbt.size != 0)
			newLine()
		stringWriter.append('}')
	}

	fun escapeString(string: String): String {
		return JsonPrimitive(string).toString()
	}

	fun escapeName(key: String): String =
		if (key.matches(SIMPLE_NAME)) key else escapeString(key)

	fun writeName(key: String) {
		stringWriter.append(escapeName(key))
		stringWriter.append(':')
		if (!compact) stringWriter.append(' ')
	}
}
