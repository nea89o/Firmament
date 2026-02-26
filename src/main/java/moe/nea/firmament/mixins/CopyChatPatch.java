package moe.nea.firmament.mixins;

import moe.nea.firmament.features.chat.CopyChat;
import moe.nea.firmament.mixins.accessor.AccessorChatHud;
import moe.nea.firmament.util.ClipboardUtils;
import moe.nea.firmament.util.MC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;

@Mixin(ChatScreen.class)
public class CopyChatPatch {
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void onRightClick(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
		if (click.button() != 1 || !CopyChat.TConfig.INSTANCE.getCopyChat()) return;
		Minecraft client = Minecraft.getInstance();
		ChatComponent chatHud = client.gui.getChat();
		var collector = new CopyChat.HoveredTextLineCollector((int) click.x(), (int) click.y());
		chatHud.captureClickableText(collector,
			MC.INSTANCE.getWindow().getGuiScaledHeight(), MC.INSTANCE.getInstance().gui.getGuiTicks(), true);
		if (collector.getResult() == null) return;
		String text = CopyChat.INSTANCE.orderedTextToString(collector.getResult());
		ClipboardUtils.INSTANCE.setTextContent(text);
		chatHud.addMessage(Component.literal("Copied: ").append(text).withStyle(ChatFormatting.GRAY));
		cir.setReturnValue(true);
		cir.cancel();
	}
}
