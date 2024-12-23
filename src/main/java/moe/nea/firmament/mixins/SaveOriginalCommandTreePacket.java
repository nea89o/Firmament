package moe.nea.firmament.mixins;

import moe.nea.firmament.features.chat.QuickCommands;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class SaveOriginalCommandTreePacket {
	@Inject(method = "onCommandTree", at = @At(value = "RETURN"))
	private void saveUnmodifiedCommandTree(CommandTreeS2CPacket packet, CallbackInfo ci) {
		QuickCommands.INSTANCE.setLastReceivedTreePacket(packet);
	}
}
