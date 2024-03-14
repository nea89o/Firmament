/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import moe.nea.firmament.repo.RepoModResourcePack;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ReloadableResourceManagerImpl.class)
public class BandAidResourcePackPatch {

    @ModifyReturnValue(
        method = "getResource",
        at = @At("RETURN")
    )
    private Optional<Resource> injectOurCustomResourcesInCaseExistingMethodsFailed(Optional<Resource> original, @Local Identifier identifier) {
        return original.or(() -> RepoModResourcePack.Companion.createResourceDirectly(identifier));
    }
}
