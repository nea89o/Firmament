/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins.custommodels;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.CustomModelOverrideParser;
import moe.nea.firmament.features.texturepack.ModelOverrideData;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(ModelOverride.Deserializer.class)
public class PatchOverrideDeserializer {

    @ModifyReturnValue(
        method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/render/model/json/ModelOverride;",
        at = @At(value = "RETURN"))
    private ModelOverride addCustomOverrides(ModelOverride original, @Local JsonObject jsonObject) {
        var originalData = (ModelOverrideData) original;
        originalData.setFirmamentOverrides(CustomModelOverrideParser.parseCustomModelOverrides(jsonObject));
        return original;
    }

    @ModifyExpressionValue(
        method = "deserializeMinPropertyValues(Lcom/google/gson/JsonObject;)Ljava/util/List;",
        at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getValue()Ljava/lang/Object;"))
    private Object removeFirmamentPredicatesFromJsonIteration(Object original, @Local Map.Entry<String, JsonElement> entry) {
        if (entry.getKey().startsWith("firmament:")) return new JsonPrimitive(0F);
        return original;
    }

    @Inject(
        method = "deserializeMinPropertyValues",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;")
    )
    private void whatever(JsonObject object, CallbackInfoReturnable<List<ModelOverride.Condition>> cir,
                          @Local Map<Identifier, Float> maps) {
        maps.entrySet().removeIf(it -> it.getKey().getNamespace().equals("firmament"));
    }
}
