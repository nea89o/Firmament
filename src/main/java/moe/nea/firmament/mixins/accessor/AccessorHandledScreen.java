/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.mixins.accessor;

import me.shedaniel.math.Rectangle;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface AccessorHandledScreen {
    @Accessor("focusedSlot")
    @Nullable
    Slot getFocusedSlot_Firmament();

    @Accessor("backgroundWidth")
    int getBackgroundWidth_Firmament();

    @Accessor("backgroundHeight")
    int getBackgroundHeight_Firmament();

    @Accessor("x")
    int getX_Firmament();

    @Accessor("y")
    int getY_Firmament();


}
