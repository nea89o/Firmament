
package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.Firmament;
import moe.nea.firmament.features.texturepack.BakedOverrideData;
import moe.nea.firmament.features.texturepack.CustomSkyBlockTextures;
import moe.nea.firmament.features.texturepack.FirmamentModelPredicate;
import moe.nea.firmament.features.texturepack.ModelOverrideData;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;
import java.util.Objects;

@Mixin(ModelOverrideList.class)
public class TestForFirmamentOverridePredicatesPatch {

	@Shadow
	private Identifier[] conditionTypes;

	@ModifyArg(method = "<init>(Lnet/minecraft/client/render/model/Baker;Ljava/util/List;)V",
		at = @At(
			value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
		))
	public Object onInit(
		Object element,
		@Local ModelOverride modelOverride
	) {
		var bakedOverride = (ModelOverrideList.BakedOverride) element;
		var modelOverrideData = ModelOverrideData.cast(modelOverride);
		BakedOverrideData.cast(bakedOverride)
		                 .setFirmamentOverrides(modelOverrideData.getFirmamentOverrides());
		if (conditionTypes.length == 0 &&
			    modelOverrideData.getFirmamentOverrides() != null &&
			    modelOverrideData.getFirmamentOverrides().length > 0) {
			conditionTypes = new Identifier[]{Firmament.INSTANCE.identifier("sentinel/enforce_model_override_evaluation")};
		}
		return element;
	}

	@ModifyExpressionValue(method = "getModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/json/ModelOverrideList$BakedOverride;test([F)Z"))
	public boolean testFirmamentOverrides(boolean originalValue,
	                                      @Local ModelOverrideList.BakedOverride bakedOverride,
	                                      @Local(argsOnly = true) ItemStack stack) {
		if (!originalValue) return false;
		var overrideData = (BakedOverrideData) (Object) bakedOverride;
		var overrides = overrideData.getFirmamentOverrides();
		if (overrides == null) return true;
		if (!CustomSkyBlockTextures.TConfig.INSTANCE.getEnableModelOverrides()) return false;
		for (FirmamentModelPredicate firmamentOverride : overrides) {
			if (!firmamentOverride.test(stack))
				return false;
		}
		return true;
	}
}
