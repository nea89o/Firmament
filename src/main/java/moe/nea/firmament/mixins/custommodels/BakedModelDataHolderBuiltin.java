
package moe.nea.firmament.mixins.custommodels;

import moe.nea.firmament.features.texturepack.BakedModelExtra;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BuiltinBakedModel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BuiltinBakedModel.class)
public class BakedModelDataHolderBuiltin implements BakedModelExtra {

    @Unique
    private BakedModel headModel;


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
