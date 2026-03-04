// SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
//
// SPDX-License-Identifier: GPL-3.0-or-later

package moe.nea.firmament.init;

import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Bridge class in the main source set so that SectionBuilderRiser can reference
 * the block render hooks via a class literal (ProGuard-tracked) rather than a
 * hardcoded string. The texturePacks source set registers its callbacks here.
 */
public class BlockRenderHooks {
    @FunctionalInterface
    public interface PatchCallback {
        BlockStateModel patch(BlockStateModel model, BlockPos pos, BlockState state);
    }

    public static Runnable enterCallback = () -> {};
    public static Runnable exitCallback = () -> {};
    public static PatchCallback patchCallback = (model, pos, state) -> model;

    public static void enterFallbackCall() {
        enterCallback.run();
    }

    public static void exitFallbackCall() {
        exitCallback.run();
    }

    public static BlockStateModel patchIndigo(BlockStateModel model, BlockPos pos, BlockState state) {
        return patchCallback.patch(model, pos, state);
    }
}
