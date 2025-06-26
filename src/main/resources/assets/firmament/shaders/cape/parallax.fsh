#version 150

#moj_import <minecraft:fog.glsl>
#define M_PI 3.1415926535897932384626433832795
#define M_TAU (2.0 * M_PI)
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler3;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float Animation;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;

out vec4 fragColor;

float highlightDistance(vec2 coord, vec2 direction, float time) {
	vec2 dir = normalize(direction);
	float projection = dot(coord, dir);
	float animationTime =  sin(projection + time * 13 * M_TAU);
	if (animationTime < 0.997) {
		return 0.0;
	}
	return animationTime;
}

void main() {
	vec4 color = texture(Sampler0, texCoord0);
	if (color.g > 0.99) {
		// TODO: maybe this speed in each direction should be a uniform
		color = texture(Sampler1, texCoord0 + Animation * vec2(3.0, -2.0));
	}

	vec4 highlightColor = texture(Sampler3, texCoord0);
	if (highlightColor.a > 0.5) {
		color = highlightColor;
		float animationHighlight = highlightDistance(texCoord0, vec2(-12.0, 2.0), Animation);
		color.rgb += (animationHighlight);
	}
	#ifdef ALPHA_CUTOUT
	if (color.a < ALPHA_CUTOUT) {
		discard;
	}
	#endif
	fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
