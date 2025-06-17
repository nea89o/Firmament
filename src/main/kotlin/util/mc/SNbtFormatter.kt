package moe.nea.firmament.util.mc

import net.minecraft.nbt.AbstractNbtList
import net.minecraft.nbt.NbtByte
import net.minecraft.nbt.NbtByteArray
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtDouble
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtEnd
import net.minecraft.nbt.NbtFloat
import net.minecraft.nbt.NbtInt
import net.minecraft.nbt.NbtIntArray
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtLong
import net.minecraft.nbt.NbtLongArray
import net.minecraft.nbt.NbtShort
import net.minecraft.nbt.NbtString
import net.minecraft.nbt.visitor.NbtElementVisitor

class SNbtFormatter private constructor() : NbtElementVisitor {
	private val result = StringBuilder()
	private var indent = 0
	private fun writeIndent() {
		result.append("\t".repeat(indent))
	}

	private fun pushIndent() {
		indent++
	}

	private fun popIndent() {
		indent--
	}

	fun apply(element: NbtElement): StringBuilder {
		element.accept(this)
		return result
	}


	override fun visitString(element: NbtString) {
		result.append(NbtString.escape(element.value))
	}

	override fun visitByte(element: NbtByte) {
		result.append(element.numberValue()).append("b")
	}

	override fun visitShort(element: NbtShort) {
		result.append(element.shortValue()).append("s")
	}

	override fun visitInt(element: NbtInt) {
		result.append(element.intValue())
	}

	override fun visitLong(element: NbtLong) {
		result.append(element.longValue()).append("L")
	}

	override fun visitFloat(element: NbtFloat) {
		result.append(element.floatValue()).append("f")
	}

	override fun visitDouble(element: NbtDouble) {
		result.append(element.doubleValue()).append("d")
	}

	private fun visitArrayContents(array: AbstractNbtList) {
		array.forEachIndexed { index, element ->
			writeIndent()
			element.accept(this)
			if (array.size() != index + 1) {
				result.append(",")
			}
			result.append("\n")
		}
	}

	private fun writeArray(arrayTypeTag: String, array: AbstractNbtList) {
		result.append("[").append(arrayTypeTag).append("\n")
		pushIndent()
		visitArrayContents(array)
		popIndent()
		writeIndent()
		result.append("]")

	}

	override fun visitByteArray(element: NbtByteArray) {
		writeArray("B;", element)
	}

	override fun visitIntArray(element: NbtIntArray) {
		writeArray("I;", element)
	}

	override fun visitLongArray(element: NbtLongArray) {
		writeArray("L;", element)
	}

	override fun visitList(element: NbtList) {
		writeArray("", element)
	}

	override fun visitCompound(compound: NbtCompound) {
		result.append("{\n")
		pushIndent()
		val keys = compound.keys.sorted()
		keys.forEachIndexed { index, key ->
			writeIndent()
			val element = compound[key] ?: error("Key '$key' found but not present in compound: $compound")
			val escapedName = escapeName(key)
			result.append(escapedName).append(": ")
			element.accept(this)
			if (keys.size != index + 1) {
				result.append(",")
			}
			result.append("\n")
		}
		popIndent()
		writeIndent()
		result.append("}")
	}

	override fun visitEnd(element: NbtEnd) {
		result.append("END")
	}

	companion object {
		fun prettify(nbt: NbtElement): String {
			return SNbtFormatter().apply(nbt).toString()
		}

		fun NbtElement.toPrettyString() = prettify(this)

		fun escapeName(key: String): String =
			if (key.matches(SIMPLE_NAME)) key else NbtString.escape(key)

		val SIMPLE_NAME = "[A-Za-z0-9._+-]+".toRegex()
	}
}
