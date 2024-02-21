/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins.custommodels;

import moe.nea.firmament.features.texturepack.FirmamentModelPredicate;
import moe.nea.firmament.features.texturepack.ModelOverrideData;
import net.minecraft.client.render.model.json.ModelOverride;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ModelOverride.class)
public class ModelOverrideDataHolder implements ModelOverrideData {

    @Unique
    private FirmamentModelPredicate[] overrides;

    @Nullable
    @Override
    public FirmamentModelPredicate[] getFirmamentOverrides() {
        return overrides;
    }

    @Override
    public void setFirmamentOverrides(@NotNull FirmamentModelPredicate[] overrides) {
        this.overrides = overrides;
    }
}
