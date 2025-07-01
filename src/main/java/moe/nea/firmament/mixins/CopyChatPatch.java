package moe.nea.firmament.mixins;

import moe.nea.firmament.features.chat.CopyChat;
import moe.nea.firmament.mixins.accessor.AccessorChatHud;
import moe.nea.firmament.util.ClipboardUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

@Mixin(ChatScreen.class)
public class CopyChatPatch {
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void onRightClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) throws NoSuchFieldException, IllegalAccessException {
		if (button != 1 || !CopyChat.TConfig.INSTANCE.getCopyChat()) return;
		MinecraftClient client = MinecraftClient.getInstance();
		ChatHud chatHud = client.inGameHud.getChatHud();
		int lineIndex = getChatLineIndex(chatHud, mouseY);
		if (lineIndex < 0) return;
		List<ChatHudLine.Visible> visible = ((AccessorChatHud) chatHud).getVisibleMessages_firmament();
		if (lineIndex >= visible.size()) return;
		ChatHudLine.Visible line = visible.get(lineIndex);
		String text = CopyChat.INSTANCE.orderedTextToString(line.content());
		ClipboardUtils.INSTANCE.setTextContent(text);
		chatHud.addMessage(Text.literal("Copied: ").append(text).formatted(Formatting.GRAY));
		cir.setReturnValue(true);
		cir.cancel();
	}

	@Unique
	private int getChatLineIndex(ChatHud chatHud, double mouseY) {
		double chatLineY = ((AccessorChatHud) chatHud).toChatLineY_firmament(mouseY);
		return MathHelper.floor(chatLineY + ((AccessorChatHud) chatHud).getScrolledLines_firmament());
	}
}
