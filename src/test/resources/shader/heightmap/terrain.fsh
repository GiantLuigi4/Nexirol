#version 450
#extension GL_EXT_shader_explicit_arithmetic_types: enable

layout (location = 0) in vec3 wsCoord;
layout (location = 1) in vec2 uv;

layout (location = 0) out vec4 colorOut;
layout (location = 1) out vec4 normalOut;

layout (binding = 0) uniform Matrices {
    mat4 projectionMatrix;
    mat4 modelViewMatrix;
};
layout (binding = 1) uniform HeightmapData {
    uniform vec2 PositionOffset;
    uniform vec2 heightRange;
};

layout (set = 1, binding = 0) uniform sampler2D heightmapSampler;

// noise
#include <shader/util/noise/hash.glsl>
#include <shader/util/noise/simple.glsl>

// normals
#include <shader/util/math/normals.glsl>

// heightmap
#include <shader/heightmap/sample_hm.glsl>

const vec4 grassGreen = vec4(128. / 255., 154. / 255., 128. / 255., 255. / 255.);
const vec4 stoneGray = vec4(128. / 255., 128. / 255., 130. / 255., 255. / 255.);

const vec3 v101 = vec3(1, 0, 1);

vec3 crd(vec2 texel, vec2 centre, vec2 offset) {
    return wsCoord * v101 + vec3(
        offset.x * 2.0,
        sampleHm(centre + (offset * texel)),
        offset.y * 2.0
    );
}

const vec3 constantVec = vec3(0.75, 0.9, -0.5) * 300.;
const vec3 constantVecNormalized = normalize(constantVec);

void main() {
    const ivec2 tSize = textureSize(heightmapSampler, 0);
    const vec2 texel = 1. / vec2(
        float(tSize.x),
        float(tSize.y)
    );

    vec3 normalUR = calculateNormal(
        //wsCoord,
        crd(texel, uv, vec2(0)),
        crd(texel, uv, vec2(1, 0)),
        crd(texel, uv, vec2(0, 1))
    );
    normalUR.y = abs(normalUR.y);
    const vec3 normal = normalize(normalUR);

    normalOut = vec4(normal, 1.0);

    const float amt = dot(normal, constantVecNormalized);
    const float dAdd = 0.;

    const float nz = rand(mod(mod(round(wsCoord.xz), 64.0) + round(wsCoord.xz / 3.), 128));
    const float sinValue = sin(normal.y);
    const vec4 grassColor = vec4((1 - nz) / 8, nz / 2 + 0.5, nz / 8, 1);
    const vec4 stoneColor = vec4(nz.xxx, 1);

    colorOut = mix(stoneColor, grassColor, step(0.5, sinValue));
    const vec4 color = mix(stoneGray, grassGreen, step(0.5, sinValue));

    colorOut *= color;
    colorOut = colorOut * vec4((amt * (1 - (dAdd / 1000.))).xxx, 1.);
}
