
package moe.nea.firmament.mixins.custommodels;

import com.google.gson.annotations.SerializedName;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.BakedModelExtra;
import moe.nea.firmament.features.texturepack.JsonUnbakedModelFirmExtra;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;
import java.util.Objects;

@Mixin(JsonUnbakedModel.class)
public class JsonUnbakedModelDataHolder implements JsonUnbakedModelFirmExtra {
    @Shadow
    @Nullable
    protected JsonUnbakedModel parent;
    @Unique
    @Nullable
    public Identifier headModel;

    @Override
    public void setHeadModel_firmament(@Nullable Identifier identifier) {
        this.headModel = identifier;
    }

    @Override
    public @Nullable Identifier getHeadModel_firmament() {
        if (this.headModel != null) return this.headModel;
        if (this.parent == null) return null;
        return ((JsonUnbakedModelFirmExtra) this.parent).getHeadModel_firmament();
    }

    @ModifyReturnValue(method = "getModelDependencies", at = @At("RETURN"))
    private Collection<Identifier> addDependencies(Collection<Identifier> original) {
        var headModel = getHeadModel_firmament();
        if (headModel != null) {
            original.add(headModel);
        }
        return original;
    }

    @ModifyReturnValue(
        method = "bake(Lnet/minecraft/client/render/model/Baker;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Z)Lnet/minecraft/client/render/model/BakedModel;",
        at = @At(value = "RETURN"))
    private BakedModel bakeExtraInfo(BakedModel original, @Local(argsOnly = true) Baker baker) {
        var headModel = getHeadModel_firmament();
        if (headModel != null && original instanceof BakedModelExtra extra) {
            UnbakedModel unbakedModel = baker.getOrLoadModel(headModel);
            extra.setHeadModel_firmament(
                Objects.equals(unbakedModel, parent)
                    ? null
                    : baker.bake(headModel, ModelRotation.X0_Y0));
        }
        return original;
    }
}
