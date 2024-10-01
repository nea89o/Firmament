package moe.nea.firmament.util.accessors

import net.minecraft.client.gui.hud.ChatHud
import net.minecraft.client.gui.hud.ChatHudLine
import moe.nea.firmament.mixins.accessor.AccessorChatHud

val ChatHud.messages: MutableList<ChatHudLine>
	get() = (this as AccessorChatHud).messages_firmament
