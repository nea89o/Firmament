package moe.nea.firmament.mixins.accessor;

import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractSignEditScreen.class)
public interface AccessorSignEditScreen
{
    @Accessor("messages")
    String[] getMessages_Firmament();

    @Accessor("messages")
    void setMessages_Firmament(String[] messages);

    @Accessor("sign")
    SignBlockEntity getSign_Firmament();

    @Invoker("onDone")
    void invokeOnDone_Firmament();
}
