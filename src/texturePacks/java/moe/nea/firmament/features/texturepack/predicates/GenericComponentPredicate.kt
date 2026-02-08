package moe.nea.firmament.features.texturepack.predicates

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import kotlin.jvm.optionals.getOrNull
import net.minecraft.core.component.DataComponentType
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.ResourceKey
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import moe.nea.firmament.features.texturepack.FirmamentModelPredicate
import moe.nea.firmament.features.texturepack.FirmamentModelPredicateParser
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.mc.NbtPrism
import moe.nea.firmament.util.mc.unsafeNbt

data class GenericComponentPredicate<T : Any>(
	val componentType: DataComponentType<T>,
	val codec: Codec<T>,
	val path: NbtPrism,
	val matcher: NbtMatcher,
) : FirmamentModelPredicate {
	constructor(componentType: DataComponentType<T>, path: NbtPrism, matcher: NbtMatcher)
		: this(componentType, componentType.codecOrThrow(), path, matcher)

	override fun test(stack: ItemStack, holder: LivingEntity?): Boolean {
		val component = stack.get(componentType) ?: return false
		// TODO: cache this
		val nbt =
			if (component is CustomData) component.unsafeNbt
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
				.lookupOrThrow(Registries.DATA_COMPONENT_TYPE)
				.getOrThrow(
					ResourceKey.create(
						Registries.DATA_COMPONENT_TYPE,
						Identifier.parse(jsonElement.get("component").asString)
					)
				).value()
			return GenericComponentPredicate(component, prism, matcher)
		}
	}

}
