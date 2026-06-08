#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 InSize;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 texel = 1.0 / InSize;

    vec3 tl = texture(DiffuseSampler, texCoord + texel * vec2(-1.0, -1.0)).rgb;
    vec3  l = texture(DiffuseSampler, texCoord + texel * vec2(-1.0,  0.0)).rgb;
    vec3 bl = texture(DiffuseSampler, texCoord + texel * vec2(-1.0,  1.0)).rgb;

    vec3  t = texture(DiffuseSampler, texCoord + texel * vec2( 0.0, -1.0)).rgb;
    vec3  b = texture(DiffuseSampler, texCoord + texel * vec2( 0.0,  1.0)).rgb;

    vec3 tr = texture(DiffuseSampler, texCoord + texel * vec2( 1.0, -1.0)).rgb;
    vec3  r = texture(DiffuseSampler, texCoord + texel * vec2( 1.0,  0.0)).rgb;
    vec3 br = texture(DiffuseSampler, texCoord + texel * vec2( 1.0,  1.0)).rgb;

    vec3 gx = -tl - 2.0 * l - bl + tr + 2.0 * r + br;
    vec3 gy = -tl - 2.0 * t - tr + bl + 2.0 * b + br;

    float edge = length(gx) + length(gy);

    edge *= 1.5;

    fragColor = vec4(
        edge * 0.15,
        edge * 0.35,
        edge,
        1.0
    );
}