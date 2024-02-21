/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins.custommodels;

import moe.nea.firmament.features.texturepack.BakedOverrideData;
import moe.nea.firmament.features.texturepack.FirmamentModelPredicate;
import net.minecraft.client.render.model.json.ModelOverrideList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ModelOverrideList.BakedOverride.class)
public class BakedOverrideDataHolder implements BakedOverrideData {

    @Unique
    private FirmamentModelPredicate[] firmamentOverrides;

    @Nullable
    @Override
    public FirmamentModelPredicate[] getFirmamentOverrides() {
        return firmamentOverrides;
    }

    @Override
    public void setFirmamentOverrides(@NotNull FirmamentModelPredicate[] overrides) {
        this.firmamentOverrides = overrides;
    }
}
