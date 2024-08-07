package moe.nea.firmament.mixins;

import moe.nea.firmament.events.BakeExtraModelsEvent;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelLoader.class)
public abstract class CustomModelBakerPatch {

    @Shadow
    @Final
    private Map<ModelIdentifier, UnbakedModel> modelsToBake;

    @Shadow
    protected abstract void loadItemModel(ModelIdentifier id);

    @Shadow
    abstract UnbakedModel getOrLoadModel(Identifier id);

    @Shadow
    protected abstract void add(ModelIdentifier id, UnbakedModel model);

    @Unique
    private void loadNonItemModel(ModelIdentifier identifier) {
        UnbakedModel unbakedModel = this.getOrLoadModel(identifier.id());
        this.add(identifier, unbakedModel);
    }


    @Inject(method = "bake", at = @At("HEAD"))
    public void onBake(ModelLoader.SpriteGetter spliteGetter, CallbackInfo ci) {
        BakeExtraModelsEvent.Companion.publish(new BakeExtraModelsEvent(this::loadItemModel, this::loadNonItemModel));
        modelsToBake.values().forEach(model -> model.setParents(this::getOrLoadModel));
//        modelsToBake.keySet().stream()
//            .filter(it -> !it.id().getNamespace().equals("minecraft"))
//            .forEach(it -> System.out.println("Non minecraft texture is being loaded: " + it));
    }
}
