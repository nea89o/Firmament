package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.features.texturepack.BakedModelExtra;
import moe.nea.firmament.features.texturepack.JsonUnbakedModelFirmExtra;
import moe.nea.firmament.features.texturepack.TintOverrides;
import moe.nea.firmament.util.ErrorUtil;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(JsonUnbakedModel.class)
public abstract class JsonUnbakedModelDataHolder implements JsonUnbakedModelFirmExtra {
	@Shadow
	@Nullable
	protected JsonUnbakedModel parent;

	@Shadow
	public abstract String toString();

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

	@Inject(method = "resolve", at = @At("HEAD"))
	private void addDependencies(UnbakedModel.Resolver resolver, CallbackInfo ci) {
		var headModel = getHeadModel_firmament();
		if (headModel != null) {
			resolver.resolve(headModel);
		}
	}

	private void addExtraBakeInfo(BakedModel bakedModel, Baker baker) {
		if (!this.toString().contains("minecraft") && this.toString().contains("crimson")) {
			System.out.println("Found non minecraft model " + this);
		}
		var extra = BakedModelExtra.cast(bakedModel);
		if (extra != null) {
			var headModel = getHeadModel_firmament();
			if (headModel != null) {
				extra.setHeadModel_firmament(baker.bake(headModel, ModelRotation.X0_Y0));
			}
			if (getTintOverrides_firmament().hasOverrides()) {
				extra.setTintOverrides_firmament(getTintOverrides_firmament());
			}
		}
	}

	/**
	 * @see ProvideBakerToJsonUnbakedModelPatch
	 */
	@Override
	public void storeExtraBaker_firmament(@NotNull Baker baker) {
		this.storedBaker = baker;
	}

	@Unique
	private Baker storedBaker;

	@ModifyReturnValue(
		method = "bake(Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Z)Lnet/minecraft/client/render/model/BakedModel;",
		at = @At("RETURN"))
	private BakedModel bakeExtraInfoWithoutBaker(BakedModel original) {
		if (storedBaker != null) {
			addExtraBakeInfo(original, storedBaker);
			storedBaker = null;
		}
		return original;
	}

	@ModifyReturnValue(
		method = {
			"bake(Lnet/minecraft/client/render/model/Baker;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;)Lnet/minecraft/client/render/model/BakedModel;"
		},
		at = @At(value = "RETURN"))
	private BakedModel bakeExtraInfo(BakedModel original, @Local(argsOnly = true) Baker baker) {
		addExtraBakeInfo(original, baker);
		return original;
	}
}
