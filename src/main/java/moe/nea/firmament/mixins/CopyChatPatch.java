package moe.nea.firmament.mixins;

import moe.nea.firmament.features.chat.CopyChat;
import moe.nea.firmament.util.ClipboardUtils;
import moe.nea.firmament.util.MC;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class CopyChatPatch {
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void onRightClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
		if (event.button() != 1 || !CopyChat.TConfig.INSTANCE.getCopyChat()) return;
		Minecraft client = Minecraft.getInstance();
		ChatComponent chatHud = client.gui.getChat();
		var collector = new CopyChat.HoveredTextLineCollector((int) event.x(), (int) event.y());
		chatHud.captureClickableText(collector,
			MC.INSTANCE.getWindow().getGuiScaledHeight(), MC.INSTANCE.getInstance().gui.getGuiTicks(), ChatComponent.DisplayMode.FOREGROUND);
		if (collector.getResult() == null) return;
		String text = CopyChat.INSTANCE.orderedTextToString(collector.getResult());
		ClipboardUtils.INSTANCE.setTextContent(text);
		chatHud.addClientSystemMessage(Component.literal("Copied: ").append(text).withStyle(ChatFormatting.GRAY));
		cir.setReturnValue(true);
		cir.cancel();
	}
}
