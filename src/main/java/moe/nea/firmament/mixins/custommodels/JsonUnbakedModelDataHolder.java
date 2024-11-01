package moe.nea.firmament.mixins.custommodels;

import com.google.gson.annotations.SerializedName;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.BakedModelExtra;
import moe.nea.firmament.features.texturepack.JsonUnbakedModelFirmExtra;
import moe.nea.firmament.features.texturepack.TintOverrides;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
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
	@Unique
	@Nullable
	public TintOverrides tintOverrides;
	@Unique
	@Nullable
	public TintOverrides mergedTintOverrides;

	@Override
	public void setTintOverrides_firmament(@Nullable TintOverrides tintOverrides) {
		this.tintOverrides = tintOverrides;
		this.mergedTintOverrides = null;
	}

	@Override
	public @NotNull TintOverrides getTintOverrides_firmament() {
		if (mergedTintOverrides != null)
			return mergedTintOverrides;
		var mergedTintOverrides = parent == null ? new TintOverrides()
			: ((JsonUnbakedModelFirmExtra) parent).getTintOverrides_firmament();
		if (tintOverrides != null)
			mergedTintOverrides = tintOverrides.mergeWithParent(mergedTintOverrides);
		this.mergedTintOverrides = mergedTintOverrides;
		return mergedTintOverrides;
	}

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
		if (original instanceof BakedModelExtra extra) {
			var headModel = getHeadModel_firmament();
			if (headModel != null) {
				UnbakedModel unbakedModel = baker.getOrLoadModel(headModel);
				extra.setHeadModel_firmament(
					Objects.equals(unbakedModel, parent)
						? null
						: baker.bake(headModel, ModelRotation.X0_Y0));
			}
			if (getTintOverrides_firmament().hasOverrides())
				extra.setTintOverrides_firmament(getTintOverrides_firmament());
		}
		return original;
	}
}
