package moe.nea.firmament.features.texturepack.predicates

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import kotlin.jvm.optionals.getOrNull
import net.minecraft.component.ComponentType
import net.minecraft.component.type.NbtComponent
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import moe.nea.firmament.features.texturepack.FirmamentModelPredicate
import moe.nea.firmament.features.texturepack.FirmamentModelPredicateParser
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.mc.NbtPrism

data class GenericComponentPredicate<T>(
	val componentType: ComponentType<T>,
	val codec: Codec<T>,
	val path: NbtPrism,
	val matcher: NbtMatcher,
) : FirmamentModelPredicate {
	constructor(componentType: ComponentType<T>, path: NbtPrism, matcher: NbtMatcher)
		: this(componentType, componentType.codecOrThrow, path, matcher)

	override fun test(stack: ItemStack, holder: LivingEntity?): Boolean {
		val component = stack.get(componentType) ?: return false
		// TODO: cache this
		val nbt =
			if (component is NbtComponent) component.nbt
			else codec.encodeStart(NbtOps.INSTANCE, component)
				.resultOrPartial().getOrNull() ?: return false
		return path.access(nbt).any { matcher.matches(it) }
	}

	object Parser : FirmamentModelPredicateParser {
		override fun parse(jsonElement: JsonElement): GenericComponentPredicate<*>? {
			if (jsonElement !is JsonObject) return null
			val path = jsonElement.get("path") ?: return null
			val prism = NbtPrism.fromElement(path) ?: return null
			val matcher = NbtMatcher.Parser.parse(jsonElement.get("match") ?: jsonElement)
				?: return null
			val component = MC.currentOrDefaultRegistries
				.getOrThrow(RegistryKeys.DATA_COMPONENT_TYPE)
				.getOrThrow(
					RegistryKey.of(
						RegistryKeys.DATA_COMPONENT_TYPE,
						Identifier.of(jsonElement.get("component").asString)
					)
				).value()
			return GenericComponentPredicate(component, prism, matcher)
		}
	}

}
