
package moe.nea.firmament.mixins.accessor;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractClientPlayerEntity.class)
public interface AccessorAbstractClientPlayerEntity {
    @Accessor("playerListEntry")
    void setPlayerListEntry_firmament(PlayerListEntry playerListEntry);
}
