/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins;

import moe.nea.firmament.events.BakeExtraModelsEvent;
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
        BakeExtraModelsEvent.Companion.publish(new BakeExtraModelsEvent(this::addModel));
        modelsToBake.values().forEach(model -> model.setParents(this::getOrLoadModel));
        modelsToBake.keySet().stream()
            .filter(it -> !it.getNamespace().equals("minecraft"))
            .forEach(it -> System.out.println("Non minecraft texture is being loaded: " + it));
    }
}
