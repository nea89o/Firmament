/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * SPDX-FileCopyrightText: 2025 azureaaron via Skyblocker
 */

package moe.nea.firmament.mixins.render.renderer;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import moe.nea.firmament.util.render.MultiSpecialGuiRenderState;
import moe.nea.firmament.util.render.MultiSpecialGuiRenderer;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * The structure of this class was roughly taken from SkyBlocker, retrieved 29.07.2025
 */
@Mixin(GuiRenderer.class)
public class MultipleSpecialGuiRenderStates {
	@Shadow
	@Final
	private MultiBufferSource.BufferSource bufferSource;
	@Shadow
	@Final
	private GuiRenderState renderState;
	@Unique
	Map<MultiSpecialGuiRenderState, MultiSpecialGuiRenderer<?>> multiRenderers = new HashMap<>();

	@Inject(method = "preparePictureInPictureState", at = @At("HEAD"), cancellable = true)
	private <T extends PictureInPictureRenderState> void onPrepareElement(T picturesInPictureState, int guiScale, CallbackInfo ci) {
		if (picturesInPictureState instanceof MultiSpecialGuiRenderState multiState) {
			@SuppressWarnings({"resource", "unchecked"})
			var renderer = (PictureInPictureRenderer<T>) multiRenderers
				.computeIfAbsent(multiState, elementState$ -> elementState$.createRenderer(this.bufferSource));
			renderer.prepare(picturesInPictureState, renderState, guiScale);
			ci.cancel();
		}
	}

	@Inject(method = "close", at = @At("TAIL"))
	private void onClose(CallbackInfo ci) {
		multiRenderers.values().forEach(PictureInPictureRenderer::close);
	}

	@Inject(method = "render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;clearUnusedOversizedItemRenderers()V"))
	private void onAfterRender(GpuBufferSlice fogBuffer, CallbackInfo ci) {
		multiRenderers.values().removeIf(it -> {
			if (it.consumeRender()) {
				return false;
			} else {
				it.close();
				return true;
			}
		});
	}
}
