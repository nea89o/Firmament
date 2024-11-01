
package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.JsonUnbakedModelFirmExtra;
import net.minecraft.client.render.model.json.ItemModelGenerator;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemModelGenerator.class)
public class ItemModelGeneratorJsonUnbakedModelCopy {
    @ModifyReturnValue(method = "create", at = @At("RETURN"))
    private JsonUnbakedModel copyExtraModelData(JsonUnbakedModel original, @Local(argsOnly = true) JsonUnbakedModel oldModel) {
        var extra = ((JsonUnbakedModelFirmExtra) original);
		var oldExtra = ((JsonUnbakedModelFirmExtra) oldModel);
		extra.setHeadModel_firmament(oldExtra.getHeadModel_firmament());
		extra.setTintOverrides_firmament(oldExtra.getTintOverrides_firmament());
        return original;
    }
}
