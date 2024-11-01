
package moe.nea.firmament.mixins.custommodels;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.JsonUnbakedModelFirmExtra;
import moe.nea.firmament.features.texturepack.TintOverrides;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(JsonUnbakedModel.Deserializer.class)
public class PatchJsonUnbakedModelDeserializer {
	@ModifyReturnValue(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/render/model/json/JsonUnbakedModel;",
		at = @At("RETURN"))
	private JsonUnbakedModel addHeadModel(JsonUnbakedModel original, @Local JsonObject jsonObject) {
		var headModel = jsonObject.get("firmament:head_model");
		var extra = ((JsonUnbakedModelFirmExtra) original);
		if (headModel instanceof JsonPrimitive prim && prim.isString()) {
			extra.setHeadModel_firmament(Identifier.of(prim.getAsString()));
		}
		var tintOverrides = jsonObject.get("firmament:tint_overrides");
		if (tintOverrides instanceof JsonObject object) {
			extra.setTintOverrides_firmament(TintOverrides.Companion.parse(object));
		}
		return original;
	}
}
