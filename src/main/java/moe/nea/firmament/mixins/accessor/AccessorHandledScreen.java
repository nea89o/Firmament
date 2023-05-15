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
    Slot getFocusedSlot_NEU();
}
