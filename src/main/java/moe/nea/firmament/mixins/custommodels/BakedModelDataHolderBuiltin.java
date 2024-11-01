
package moe.nea.firmament.mixins.custommodels;

import moe.nea.firmament.features.texturepack.BakedModelExtra;
import moe.nea.firmament.features.texturepack.TintOverrides;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BuiltinBakedModel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BuiltinBakedModel.class)
public class BakedModelDataHolderBuiltin implements BakedModelExtra {

	@Unique
	@Nullable
	private BakedModel headModel;

	@Unique
	@Nullable
	private TintOverrides tintOverrides;

	@Override
	public @Nullable TintOverrides getTintOverrides_firmament() {
		return tintOverrides;
	}

	@Override
	public void setTintOverrides_firmament(@Nullable TintOverrides tintOverrides) {
		this.tintOverrides = tintOverrides;
	}

	@Nullable
	@Override
	public BakedModel getHeadModel_firmament() {
		return headModel;
	}

	@Override
	public void setHeadModel_firmament(@Nullable BakedModel headModel) {
		this.headModel = headModel;
	}
}
