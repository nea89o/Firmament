package moe.nea.firmament.mixins.accessor;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AccessorHandledScreen {
    @Accessor("hoveredSlot")
    @Nullable
    Slot getFocusedSlot_Firmament();

    @Accessor("imageWidth")
    int getBackgroundWidth_Firmament();

    @Accessor("imageWidth")
    @Mutable
    void setBackgroundWidth_Firmament(int newBackgroundWidth);

    @Accessor("imageHeight")
    int getBackgroundHeight_Firmament();

    @Accessor("imageHeight")
    @Mutable
    void setBackgroundHeight_Firmament(int newBackgroundHeight);

    @Accessor("leftPos")
    int getX_Firmament();

    @Accessor("leftPos")
    void setX_Firmament(int newX);

    @Accessor("topPos")
    int getY_Firmament();

    @Accessor("topPos")
    void setY_Firmament(int newY);
}
