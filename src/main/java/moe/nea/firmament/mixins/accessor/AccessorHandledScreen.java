package moe.nea.firmament.mixins.accessor;

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

    @Accessor("backgroundWidth")
    void setBackgroundWidth_Firmament(int newBackgroundWidth);

    @Accessor("backgroundHeight")
    int getBackgroundHeight_Firmament();

    @Accessor("backgroundHeight")
    void setBackgroundHeight_Firmament(int newBackgroundHeight);

    @Accessor("x")
    int getX_Firmament();

    @Accessor("x")
    void setX_Firmament(int newX);

    @Accessor("y")
    int getY_Firmament();

    @Accessor("y")
    void setY_Firmament(int newY);

}
