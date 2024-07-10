/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins.custommodels;

import moe.nea.firmament.features.texturepack.BakedModelExtra;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BasicBakedModel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BasicBakedModel.class)
public class BakedModelDataHolderBasic implements BakedModelExtra {

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
