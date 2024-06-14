#version 450
#extension GL_EXT_shader_explicit_arithmetic_types: enable

layout (location = 0) in vec3 wsCoord;
layout (location = 1) in vec2 uv;

layout (location = 0) out vec4 colorOut;
layout (location = 1) out vec4 normalOut;

vec4 screenToWorld(mat4 model, mat4 proj, in float depth, in vec2 uv) {
    vec4 coord = vec4(uv, depth, 1.0) * 2.0 - 1.0;

    coord = inverse(proj * model) * coord;
    coord /= coord.w; // linearize
    return coord;
}

layout (binding = 0) uniform Matrices {
    mat4 projectionMatrix;
    mat4 modelViewMatrix;
};

layout (set = 1, binding = 0) uniform sampler2D heightmapSampler;

// noise
//#include <shader/util/math/bitwise/rotations.glsl>
//#include <shader/util/math/bitwise/u_shir.glsl>
//#include <shader/util/noise/rand.glsl>
//#include <shader/util/noise/xor_shift.glsl>
#include <shader/util/noise/hash.glsl>
#include <shader/util/noise/simple.glsl>

// normals
#include <shader/util/math/normals.glsl>

// heightmap
#include <shader/heightmap/sample_hm.glsl>

const vec4 grassGreen = vec4(128. / 255., 154. / 255., 128. / 255., 255. / 255.);
const vec4 stoneGray = vec4(128. / 255., 128. / 255., 130. / 255., 255. / 255.);

vec3 crd(vec2 centre, vec2 offset) {
    ivec2 tSize = textureSize(heightmapSampler, 0);
    vec2 texel = 1. / vec2(
        float(tSize.x),
        float(tSize.y)
    );

    return wsCoord * vec3(1, 0, 1) + vec3(
        offset.x * 2.0,
        sampleHm(centre + (offset * texel)),
        offset.y * 2.0
    );
}

void main() {
    vec3 normalUR = calculateNormal(
        crd(uv, vec2(0)),
        crd(uv, vec2(1, 0)),
        crd(uv, vec2(0, 1))
    );
    if (normalUR.y < 0) normalUR.y *= -1;
    vec3 normal = normalize(normalUR);

    normalOut = vec4(normal, 1.0);

    vec3 constantVec = vec3(0.75, 0.9, -0.5) * 300.;
    float amt = dot(normal, abs(normalize(constantVec)));
    //amt = max(0.0, amt);
    //float dAdd = distance(wsCoord.xyz, constantVec);
    float dAdd = 0.;

    float nz = rand(mod(mod(round(wsCoord.xz), 64.0) + round(wsCoord.xz / 3.), 128));
    vec4 color;
    if (sin(normal.y) > 0.5) {
        colorOut = vec4((1 - nz) / 8, nz / 2 + 0.5, nz / 8, 1);
        color = grassGreen;
    } else {
        colorOut = vec4(nz.xxx, 1);
        color = stoneGray;
    }

    colorOut *= color;
    colorOut = colorOut * vec4(vec3(amt) * vec3(1 - (dAdd / 1000.)), 1.);
}
