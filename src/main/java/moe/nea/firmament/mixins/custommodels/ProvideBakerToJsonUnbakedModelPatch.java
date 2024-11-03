package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import moe.nea.firmament.features.texturepack.JsonUnbakedModelFirmExtra;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;

/**
 * @see JsonUnbakedModelDataHolder#storeExtraBaker_firmament
 */
@Mixin(targets = "net.minecraft.client.render.model.ModelBaker$BakerImpl")
public abstract class ProvideBakerToJsonUnbakedModelPatch implements Baker {
	@WrapOperation(method = "bake(Lnet/minecraft/client/render/model/UnbakedModel;Lnet/minecraft/client/render/model/ModelBakeSettings;)Lnet/minecraft/client/render/model/BakedModel;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/json/JsonUnbakedModel;bake(Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Z)Lnet/minecraft/client/render/model/BakedModel;"))
	private BakedModel provideExtraBakerToModel(JsonUnbakedModel instance, Function<SpriteIdentifier, Sprite> function, ModelBakeSettings modelBakeSettings, boolean bl, Operation<BakedModel> original) {
		((JsonUnbakedModelFirmExtra) instance).storeExtraBaker_firmament(this);
		return original.call(instance, function, modelBakeSettings, bl);
	}
}
