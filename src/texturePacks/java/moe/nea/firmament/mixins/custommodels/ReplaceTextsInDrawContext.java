package moe.nea.firmament.mixins.custommodels;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import moe.nea.firmament.features.texturepack.CustomTextReplacements;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.stream.Stream;

@Mixin(GuiGraphics.class)
public class ReplaceTextsInDrawContext {
	// I HATE THIS SO MUCH WHY CANT I JUST OPERATE ON ORDEREDTEXTS!!!
	// JUNE I WILL RIP ALL OF THIS OUT AND MAKE YOU REWRITE EVERYTHING
	// TODO: be in a mood to rewrite this

	@ModifyVariable(method = "drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)V", at = @At("HEAD"), argsOnly = true)
	private Component replaceTextInDrawText(Component text) {
		return CustomTextReplacements.replaceText(text);
	}

	@ModifyVariable(method = "drawCenteredString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V", at = @At("HEAD"), argsOnly = true)
	private Component replaceTextInDrawCenteredTextWithShadow(Component text) {
		return CustomTextReplacements.replaceText(text);
	}

	@ModifyVariable(method = "drawWordWrap*", at = @At("HEAD"), argsOnly = true)
	private FormattedText replaceTextInDrawWrappedText(FormattedText stringVisitable) {
		return stringVisitable instanceof Component text ? CustomTextReplacements.replaceText(text) : stringVisitable;
	}

	@ModifyExpressionValue(method = "setComponentTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/resources/Identifier;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;stream()Ljava/util/stream/Stream;"))
	private Stream<Component> replaceTextInDrawTooltipListText(Stream<Component> original) {
		return original.map(CustomTextReplacements::replaceText);
	}

	@ModifyExpressionValue(method = "setTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/resources/Identifier;)V", at = @At(value = "INVOKE", target = "Ljava/util/List;stream()Ljava/util/stream/Stream;"))
	private Stream<Component> replaceTextInDrawTooltipListTextWithOptional(Stream<Component> original) {
		return original.map(CustomTextReplacements::replaceText);
	}

	@ModifyVariable(method = "setTooltipForNextFrame(Lnet/minecraft/network/chat/Component;II)V", at = @At("HEAD"), argsOnly = true)
	private Component replaceTextInDrawTooltipSingle(Component text) {
		return CustomTextReplacements.replaceText(text);
	}

	@ModifyExpressionValue(method = "renderComponentHoverEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/HoverEvent$ShowText;value()Lnet/minecraft/network/chat/Component;"))
	private Component replaceShowTextInHover(Component text) {
		return CustomTextReplacements.replaceText(text);
	}

}
