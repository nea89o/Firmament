/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins;

import moe.nea.firmament.features.fixes.Fixes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerLeftHandedPatch {
    @Inject(
        method = "getMainArm",
        at = @At("HEAD"),
        cancellable = true
    )
    public void onGetMainArm(CallbackInfoReturnable<Arm> cir) {
        Fixes.INSTANCE.isLeftHandedHook((PlayerEntity) (Object) this, cir);
    }
}