package moe.nea.firmament.util.mc

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.serialization.JsonOps
import java.util.concurrent.CompletableFuture
import kotlin.collections.indices
import kotlin.collections.map
import kotlin.jvm.optionals.getOrNull
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtString
import moe.nea.firmament.util.Base64Util

class NbtPrism(val path: List<String>) {
	companion object {
		fun fromElement(path: JsonElement): NbtPrism? {
			if (path is JsonArray) {
				return NbtPrism(path.map { (it as JsonPrimitive).asString })
			} else if (path is JsonPrimitive && path.isString) {
				return NbtPrism(path.asString.split("."))
			}
			return null
		}
	}

	object Argument : ArgumentType<NbtPrism> {
		override fun parse(reader: StringReader): NbtPrism? {
			return fromElement(JsonPrimitive(StringArgumentType.string().parse(reader)))
		}

		override fun getExamples(): Collection<String?>? {
			return listOf("some.nbt.path", "some.other.*", "some.path.*json.in.a.json.string")
		}
	}

	override fun toString(): String {
		return "Prism($path)"
	}

	fun access(root: NbtElement): Collection<NbtElement> {
		var rootSet = mutableListOf(root)
		var switch = mutableListOf<NbtElement>()
		for (pathSegment in path) {
			if (pathSegment == ".") continue
			if (pathSegment != "*" && pathSegment.startsWith("*")) {
				if (pathSegment == "*json") {
					for (element in rootSet) {
						val eString = element.asString().getOrNull() ?: continue
						val element = Gson().fromJson(eString, JsonElement::class.java)
						switch.add(JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, element))
					}
				} else if (pathSegment == "*base64") {
					for (element in rootSet) {
						val string = element.asString().getOrNull() ?: continue
						switch.add(NbtString.of(Base64Util.decodeString(string)))
					}
				}
			}
			for (element in rootSet) {
				if (element is NbtList) {
					if (pathSegment == "*")
						switch.addAll(element)
					val index = pathSegment.toIntOrNull() ?: continue
					if (index !in element.indices) continue
					switch.add(element[index])
				}
				if (element is NbtCompound) {
					if (pathSegment == "*")
						element.keys.mapTo(switch) { element.get(it)!! }
					switch.add(element.get(pathSegment) ?: continue)
				}
			}
			val temp = switch
			switch = rootSet
			rootSet = temp
			switch.clear()
		}
		return rootSet
	}
}
