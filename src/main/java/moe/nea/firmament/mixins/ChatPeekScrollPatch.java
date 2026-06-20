package moe.nea.firmament.mixins;

import moe.nea.firmament.features.fixes.Fixes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class ChatPeekScrollPatch {

	@Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getSelectedSlot()I"), cancellable = true)
	public void onHotbarScrollWhilePeeking(long handle, double xoffset, double yoffset, CallbackInfo ci) {
		if (Fixes.INSTANCE.shouldPeekChat() && Fixes.INSTANCE.shouldScrollPeekedChat()) ci.cancel();
	}

	@ModifyVariable(method = "onScroll", at = @At(value = "STORE"), name = "wheel")
	public int onGetChatHud(int wheel) {
		if (Fixes.INSTANCE.shouldPeekChat() && Fixes.INSTANCE.shouldScrollPeekedChat())
			Minecraft.getInstance().gui.getChat().scrollChat(wheel);
		return wheel;
	}

}
