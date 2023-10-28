/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.BiFunction;

@Mixin(ModelLoader.class)
public abstract class CustomModelBakerPatch {

    @Shadow
    protected abstract void addModel(ModelIdentifier modelId);

    @Shadow
    @Final
    private Map<Identifier, UnbakedModel> modelsToBake;

    @Shadow
    public abstract UnbakedModel getOrLoadModel(Identifier id);

    @Inject(method = "bake", at = @At("HEAD"))
    public void onBake(BiFunction<Identifier, SpriteIdentifier, Sprite> spriteLoader, CallbackInfo ci) {
        Map<Identifier, Resource> resources =
            MinecraftClient.getInstance().getResourceManager().findResources("models/item", it -> "firmskyblock".equals(it.getNamespace()) && it.getPath().endsWith(".json"));
        for (Identifier identifier : resources.keySet()) {
            ModelIdentifier modelId = new ModelIdentifier("firmskyblock", identifier.getPath().substring("models/item/".length(), identifier.getPath().length() - ".json".length()), "inventory");
            addModel(modelId);
        }
        modelsToBake.values().forEach(model -> model.setParents(this::getOrLoadModel));
    }
}
