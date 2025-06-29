package moe.nea.firmament.mixins.accessor;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ChatHud.class)
public interface AccessorChatHud {
	@Accessor("messages")
	List<ChatHudLine> getMessages_firmament();

	@Accessor("visibleMessages")
	List<ChatHudLine.Visible> getVisibleMessages_firmament();

	@Accessor("scrolledLines")
	int getScrolledLines_firmament();

	@Invoker("toChatLineY")
	double toChatLineY_firmament(double y);
}
