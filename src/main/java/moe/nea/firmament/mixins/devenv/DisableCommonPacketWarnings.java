package moe.nea.firmament.mixins.devenv;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Objects;

@Mixin(ClientPlayNetworkHandler.class)
public class DisableCommonPacketWarnings {

    @Redirect(method = "onCustomPayload", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
    public void onCustomPacket(Logger instance, String s, Object o) {
        if (!Objects.equals(o, Identifier.of("badlion", "mods"))) {
            instance.warn(s, o);
        }
    }

    @Redirect(method = "onEntityPassengersSet", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;)V", remap = false))
    public void onUnknownPassenger(Logger instance, String s) {
        // Ignore passenger data for unknown entities, since HyPixel just sends a lot of those.
    }

    @Redirect(method = "onTeam", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;[Ljava/lang/Object;)V", remap = false))
    public void onOnTeam(Logger instance, String s, Object[] objects) {
        // Ignore data for unknown teams, since HyPixel just sends a lot of invalid team data.
    }

    @Redirect(method = "onPlayerList", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
    public void onOnPlayerList(Logger instance, String s, Object o) {
        // Ignore invalid player info, since HyPixel just sends a lot of invalid player info
    }

}
