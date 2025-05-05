package moe.nea.firmament.features.texturepack.predicates

import com.google.gson.JsonElement
import com.mojang.authlib.minecraft.MinecraftProfileTexture
import java.util.UUID
import kotlin.jvm.optionals.getOrNull
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import moe.nea.firmament.features.texturepack.FirmamentModelPredicate
import moe.nea.firmament.features.texturepack.FirmamentModelPredicateParser
import moe.nea.firmament.features.texturepack.StringMatcher
import moe.nea.firmament.util.mc.decodeProfileTextureProperty
import moe.nea.firmament.util.parsePotentiallyDashlessUUID

class SkullPredicate(
	val profileId: UUID?,
	val textureProfileId: UUID?,
	val skinUrl: StringMatcher?,
	val textureValue: StringMatcher?,
) : FirmamentModelPredicate {
	object Parser : FirmamentModelPredicateParser {
		override fun parse(jsonElement: JsonElement): FirmamentModelPredicate? {
			val obj = jsonElement.asJsonObject
			val profileId = obj.getAsJsonPrimitive("profileId")
				?.asString?.let(::parsePotentiallyDashlessUUID)
			val textureProfileId = obj.getAsJsonPrimitive("textureProfileId")
				?.asString?.let(::parsePotentiallyDashlessUUID)
			val textureValue = obj.get("textureValue")?.let(StringMatcher::parse)
			val skinUrl = obj.get("skinUrl")?.let(StringMatcher::parse)
			return SkullPredicate(profileId, textureProfileId, skinUrl, textureValue)
		}
	}

	override fun test(stack: ItemStack, holder: LivingEntity?): Boolean {
		if (!stack.isOf(Items.PLAYER_HEAD)) return false
		val profile = stack.get(DataComponentTypes.PROFILE) ?: return false
		val textureProperty = profile.properties["textures"].firstOrNull()
		val textureMode = lazy(LazyThreadSafetyMode.NONE) {
			decodeProfileTextureProperty(textureProperty ?: return@lazy null)
		}
		when {
			profileId != null
				&& profileId != profile.id.getOrNull() ->
				return false

			textureValue != null
				&& !textureValue.matches(textureProperty?.value ?: "") ->
				return false

			skinUrl != null
				&& !skinUrl.matches(textureMode.value?.textures?.get(MinecraftProfileTexture.Type.SKIN)?.url ?: "") ->
				return false

			textureProfileId != null
				&& textureProfileId != textureMode.value?.profileId ->
				return false

			else -> return true
		}
	}
}
