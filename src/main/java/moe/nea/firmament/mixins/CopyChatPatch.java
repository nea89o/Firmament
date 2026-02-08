package moe.nea.firmament.mixins;

import moe.nea.firmament.features.chat.CopyChat;
import moe.nea.firmament.mixins.accessor.AccessorChatHud;
import moe.nea.firmament.util.ClipboardUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ChatScreen.class)
public class CopyChatPatch {
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void onRightClick(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir) throws NoSuchFieldException, IllegalAccessException {
		if (click.button() != 1 || !CopyChat.TConfig.INSTANCE.getCopyChat()) return;
		Minecraft client = Minecraft.getInstance();
		ChatComponent chatHud = client.gui.getChat();
		int lineIndex = getChatLineIndex(chatHud, click.y());
		if (lineIndex < 0) return;
		// TODO!!! use the new visitor for this (cc: image links)
		List<GuiMessage.Line> visible = ((AccessorChatHud) chatHud).getVisibleMessages_firmament();
		if (lineIndex >= visible.size()) return;
		GuiMessage.Line line = visible.get(lineIndex);
		String text = CopyChat.INSTANCE.orderedTextToString(line.content());
		ClipboardUtils.INSTANCE.setTextContent(text);
		chatHud.addMessage(Component.literal("Copied: ").append(text).withStyle(ChatFormatting.GRAY));
		cir.setReturnValue(true);
		cir.cancel();
	}

	@Unique
	private int getChatLineIndex(ChatComponent chatHud, double mouseY) {
		return 0; /// TODO!!!!
	}
}
