package moe.nea.firmament.mixins;

import moe.nea.firmament.features.fixes.Fixes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class ChatPeekScrollPatch {

	@Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getSelectedSlot()I"), cancellable = true)
	public void onHotbarScrollWhilePeeking(long window, double horizontal, double vertical, CallbackInfo ci) {
		if (Fixes.INSTANCE.shouldPeekChat() && Fixes.INSTANCE.shouldScrollPeekedChat()) ci.cancel();
	}

	@ModifyVariable(method = "onMouseScroll", at = @At(value = "STORE"), ordinal = 0)
	public int onGetChatHud(int i) {
		if (Fixes.INSTANCE.shouldPeekChat() && Fixes.INSTANCE.shouldScrollPeekedChat())
			MinecraftClient.getInstance().inGameHud.getChatHud().scroll(i);
		return i;
	}

}
