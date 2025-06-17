#version 150

in vec4 vertexColor;
in vec2 texCoord0;

uniform vec4 ColorModulator;
uniform float InnerCutoutRadius;

out vec4 fragColor;

void main() {
	vec4 color = vertexColor;
	if (color.a == 0.0) {
		discard;
	}
	float d = length(texCoord0 - vec2(0.5));
	if (d > 0.5 || d < InnerCutoutRadius)
	{
		discard;
	}
	fragColor = color * ColorModulator;
}
