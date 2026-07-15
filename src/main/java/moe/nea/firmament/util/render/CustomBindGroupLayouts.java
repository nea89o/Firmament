package moe.nea.firmament.util.render;

import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.shaders.UniformType;

public class CustomBindGroupLayouts {
	public static final BindGroupLayout ANIMATION_DATA = BindGroupLayout.builder()
		.withUniform("Animation", UniformType.UNIFORM_BUFFER)
		.build();
}
