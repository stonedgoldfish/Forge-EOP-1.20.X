#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

float rand(vec2 co) {
    return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
}

vec3 contrast(vec3 color, vec3 midpoint, float amount) {
    return (color - midpoint) * amount + midpoint;
}

void main() {
    vec2 uv = texCoord;

    float glitchTimer = floor(Time * 6.0);

    float burstRandom = rand(vec2(glitchTimer, glitchTimer * 0.41));
    float burst = step(0.65, burstRandom);

    float baseStrength = 1.0;
    float extraStrength = burst * 0.75;
    float glitchStrength = baseStrength + extraStrength;

    float line = floor(uv.y * 180.0);
    float lineNoise = rand(vec2(line, glitchTimer));

    float tearMask = step(0.83, lineNoise);
    float tear = tearMask * (lineNoise - 0.5) * 0.10 * glitchStrength;

    float band = floor(uv.y * 18.0);
    float bandNoise = rand(vec2(band, glitchTimer * 1.7));
    float bandTear = step(0.78, bandNoise) * (bandNoise - 0.5) * 0.045 * glitchStrength;

    vec2 block = floor(uv * vec2(24.0, 14.0));
    float blockNoise = rand(block + glitchTimer);

    vec2 blockShift = vec2(
        step(0.72, blockNoise) * (blockNoise - 0.5) * 0.055,
        step(0.91, blockNoise) * (blockNoise - 0.5) * 0.035
    ) * glitchStrength;

    vec2 distortedUV = uv + vec2(tear + bandTear, 0.0) + blockShift;

    float split = 9.0 + extraStrength * 8.0;

    float r = texture(DiffuseSampler, distortedUV + vec2(split, -2.0) * oneTexel).r;
    float g = texture(DiffuseSampler, distortedUV + vec2(-4.0, 3.0) * oneTexel).g;
    float b = texture(DiffuseSampler, distortedUV + vec2(-split, 1.0) * oneTexel).b;

    vec3 color = vec3(r, g, b);

    color += sin(uv.y * 900.0 + Time * 35.0) * 0.055;

    color += extraStrength * rand(vec2(uv.y * 100.0, glitchTimer)) * 0.08;

    color = contrast(color, vec3(0.5), 1.30 + extraStrength * 0.20);

    fragColor = vec4(color, 1.0);
}