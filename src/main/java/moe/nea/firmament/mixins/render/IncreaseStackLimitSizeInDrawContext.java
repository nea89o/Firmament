package moe.nea.firmament.mixins.render;

import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GuiGraphics.class)
public class IncreaseStackLimitSizeInDrawContext {
	// [22:00:57] [Render thread/ERROR] (Minecraft) Couldn't compile program for pipeline firmament:gui_textured_overlay_tris_circle:
	// net.minecraft.client.gl.ShaderLoader$LoadException: Error encountered when linking program containing
	// VS minecraft:core/position_tex_color and FS firmament:circle_discard_color.
	// Log output: error: declarations for uniform `ColorModulator` are inside block `DynamicTransforms` and outside a block
	@ModifyArg(
		method = "<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/render/state/GuiRenderState;II)V",
		at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;<init>(I)V"))
	private static int increaseStackSize(int stackSize) {
		return Math.max(stackSize, 48);
	}
}
