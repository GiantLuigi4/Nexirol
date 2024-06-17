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

const vec3 v101 = vec3(1.0, 0.0, 1.0);

vec3 crd(const vec2 texel, const vec2 centre, const vec2 offset) {
    return wsCoord * v101 + vec3(
        offset.x,
        sampleHm(centre + (offset * texel)),
        offset.y
    );
}

const vec3 constantVec = vec3(0.75, 0.9, -0.5) * 300.;
const vec3 constantVecNormalized = normalize(constantVec);

void main() {
    const ivec2 tSize = textureSize(heightmapSampler, 0);
    const vec2 texel = 1. / tSize;

    vec3 normalUR = calculateNormal(
        //wsCoord,
        crd(texel, uv, vec2(0.0)),
        crd(texel, uv, vec2(2.0, 0.0)),
        crd(texel, uv, vec2(0.0, 2.0))
    );

    normalUR.y = abs(normalUR.y);
    const vec3 normal = normalize(normalUR);
    normalOut = vec4(normal, 1.0);

    const float amt = dot(normal, constantVecNormalized);
    const float dAdd = 0.;

    const float nz = rand(mod(mod(round(wsCoord.xz), 64.0) + round(wsCoord.xz / 3.), 128.0));
    const float sinValue = sin(normal.y);

    const vec4 grassColor = vec4((1.0 - nz) / 8.0, nz / 2.0 + 0.5, nz / 8.0, 1.0);
    const vec4 stoneColor = vec4(nz.xxx, 1.0);

    const float sv = step(0.5, sinValue);
    colorOut = mix(stoneColor, grassColor, sv);
    const vec4 color = mix(stoneGray, grassGreen, sv);

    colorOut *= color;
    colorOut = colorOut * vec4((amt * (1.0 - (dAdd / 1000.))).xxx, 1.);
}
