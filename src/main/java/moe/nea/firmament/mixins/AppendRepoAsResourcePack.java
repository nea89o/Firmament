/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins;

import moe.nea.firmament.repo.RepoModResourcePack;
import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil;
import net.minecraft.resource.ResourceType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ModResourcePackUtil.class)
public class AppendRepoAsResourcePack {
    @Inject(method = "appendModResourcePacks", at = @At("TAIL"))
    private static void onAppendModResourcePack(
        List<ModResourcePack> packs,
        ResourceType type,
        @Nullable String subPath,
        CallbackInfo ci
    ) {
        RepoModResourcePack.Companion.append(packs);
    }

}
