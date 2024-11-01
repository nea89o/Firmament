
package moe.nea.firmament.mixins.custommodels;

import moe.nea.firmament.features.texturepack.BakedModelExtra;
import moe.nea.firmament.features.texturepack.TintOverrides;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BasicBakedModel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BasicBakedModel.class)
public class BakedModelDataHolderBasic implements BakedModelExtra {

	@Unique
	private BakedModel headModel;

	@Unique
	@Nullable
	private TintOverrides tintOverrides;

	@Nullable
	@Override
	public BakedModel getHeadModel_firmament() {
		return headModel;
	}

	@Override
	public void setHeadModel_firmament(@Nullable BakedModel headModel) {
		this.headModel = headModel;
	}

	@Override
	public @Nullable TintOverrides getTintOverrides_firmament() {
		return tintOverrides;
	}

	@Override
	public void setTintOverrides_firmament(@Nullable TintOverrides tintOverrides) {
		this.tintOverrides = tintOverrides;
	}
}
